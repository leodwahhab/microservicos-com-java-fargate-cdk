package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_project01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));

        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, id) // Cria serviço ECS Fargate + ALB, Task Definition, Target Group, Listener e Security Groups
                .cluster(cluster) // Reaproveita o cluster (e a VPC dele) já existentes, em vez de criar novos
                .serviceName("service-01") // Nome do serviço ECS dentro do cluster
                .cpu(256) // 256 unidades de CPU (0,25 vCPU) por task
                .memoryLimitMiB(1024) // 1024 MiB (1 GB) de memória por task
                .desiredCount(2) // Quantidade de tasks (containers) mantidas em execução
                .listenerPort(8080) // Porta em que o listener do ALB recebe requisições
                .taskImageOptions( // Configuração do container que roda dentro da task
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project01") // Nome do container na Task Definition
                                .image(ContainerImage.fromRegistry("leodwahhab/curso_aws_project01:2.0.0")) // Imagem Docker baixada do Docker Hub
                                .containerPort(8080) // Porta exposta pelo container (destino do Target Group do ALB)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder() // Envia os logs do container para o CloudWatch Logs
                                                .logGroup(LogGroup.Builder.create(this, "Service01LogGroup") // Cria o Log Group no CloudWatch
                                                        .logGroupName("Service01") // Nome do Log Group
                                                        .removalPolicy(RemovalPolicy.DESTROY) // Remove o Log Group ao destruir a stack
                                                        .build())
                                                .streamPrefix("Service01") // Prefixo dos log streams dentro do Log Group
                                                .build())
                                ).environment(envVariables)
                                .build()
                ).assignPublicIp(true) // Atribui IP público às tasks (necessário para baixar a imagem do Docker Hub em subnet pública)
                .build();

        service01.getTargetGroup().configureHealthCheck(HealthCheck.builder() // Configura o health check do Target Group do ALB
                        .path("/actuator/health") // Endpoint que o ALB chama para verificar se a task está saudável
                        .port("8080") // Porta usada na verificação
                        .healthyHttpCodes("200") // Código HTTP que indica task saudável
                        .build());

        service01.getService()
                .autoScaleTaskCount(EnableScalingProps.builder() // Habilita o Auto Scaling (Application Auto Scaling) do serviço ECS
                        .minCapacity(2) // Mínimo de 2 tasks em execução
                        .maxCapacity(4) // Máximo de 4 tasks em execução
                        .build())
                .scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder() // Cria política de scaling baseada em CPU (alarmes no CloudWatch)
                        .targetUtilizationPercent(50) // Escala quando a CPU média ultrapassa 50%
                        .scaleInCooldown(Duration.seconds(60)) // Espera 60s após reduzir tasks antes de nova redução
                        .scaleOutCooldown(Duration.seconds(60)) // Espera 60s após aumentar tasks antes de novo aumento
                        .build());
    }
}
