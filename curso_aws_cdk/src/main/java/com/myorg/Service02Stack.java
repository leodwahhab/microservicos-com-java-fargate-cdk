package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service02Stack extends Stack {
    public Service02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productEventsTopic) {
        this(scope, id, null, cluster, productEventsTopic);
    }

    public Service02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productEventsTopic) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_project01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));
        envVariables.put("AWS_REGION", "us-east-1");
        envVariables.put("AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN", productEventsTopic.getTopic().getTopicArn());

        ApplicationLoadBalancedFargateService service02 = ApplicationLoadBalancedFargateService.Builder.create(this, id) // Cria serviço ECS Fargate + ALB, Task Definition, Target Group, Listener e Security Groups
                .cluster(cluster) // Reaproveita o cluster (e a VPC dele) já existentes, em vez de criar novos
                .serviceName("service-02") // Nome do serviço ECS dentro do cluster
                .cpu(256) // 256 unidades de CPU (0,25 vCPU) por task
                .memoryLimitMiB(1024) // 1024 MiB (1 GB) de memória por task
                .desiredCount(2) // Quantidade de tasks (containers) mantidas em execução
                .listenerPort(9090) // Porta em que o listener do ALB recebe requisições
                .taskImageOptions( // Configuração do container que roda dentro da task
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project02") // Nome do container na Task Definition
                                .image(ContainerImage.fromRegistry("leodwahhab/curso_aws_project02:1.3.0")) // Imagem Docker baixada do Docker Hub
                                .containerPort(9090) // Porta exposta pelo container (destino do Target Group do ALB)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder() // Envia os logs do container para o CloudWatch Logs
                                                .logGroup(LogGroup.Builder.create(this, "Service02LogGroup") // Cria o Log Group no CloudWatch
                                                        .logGroupName("Service02") // Nome do Log Group
                                                        .removalPolicy(RemovalPolicy.DESTROY) // Remove o Log Group ao destruir a stack
                                                        .build())
                                                .streamPrefix("Service02") // Prefixo dos log streams dentro do Log Group
                                                .build())
                                ).environment(envVariables)
                                .build()
                ).assignPublicIp(true) // Atribui IP público às tasks (necessário para baixar a imagem do Docker Hub em subnet pública)
                .build();

        service02.getTargetGroup().configureHealthCheck(HealthCheck.builder() // Configura o health check do Target Group do ALB
                        .path("/actuator/health") // Endpoint que o ALB chama para verificar se a task está saudável
                        .port("9090") // Porta usada na verificação
                        .healthyHttpCodes("200") // Código HTTP que indica task saudável
                        .build());

        service02.getService()
                .autoScaleTaskCount(EnableScalingProps.builder() // Habilita o Auto Scaling (Application Auto Scaling) do serviço ECS
                        .minCapacity(2) // Mínimo de 2 tasks em execução
                        .maxCapacity(4) // Máximo de 4 tasks em execução
                        .build())
                .scaleOnCpuUtilization("Service02AutoScaling", CpuUtilizationScalingProps.builder() // Cria política de scaling baseada em CPU (alarmes no CloudWatch)
                        .targetUtilizationPercent(50) // Escala quando a CPU média ultrapassa 50%
                        .scaleInCooldown(Duration.seconds(60)) // Espera 60s após reduzir tasks antes de nova redução
                        .scaleOutCooldown(Duration.seconds(60)) // Espera 60s após aumentar tasks antes de novo aumento
                        .build());

        productEventsTopic.getTopic().grantPublish(service02.getTaskDefinition().getTaskRole());
    }
}
