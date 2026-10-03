package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class RdsStack extends Stack { // Stack do CDK que provisiona o banco de dados RDS
    public RdsStack(final Construct scope, final String id, Vpc vpc) { // Construtor recebe escopo, id da stack e a VPC onde o RDS será criado
        super(scope, id); // Chama o construtor da classe Stack

        CfnParameter databasePassword = CfnParameter.Builder.create(this, "databasePassword") // Cria um parâmetro do CloudFormation para a senha do banco
                .type("String") // Define o tipo do parâmetro como String
                .description("RDS password") // Descrição exibida ao informar o parâmetro
                .build(); // Constrói o parâmetro

        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup()); // Importa o security group padrão da VPC
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306)); // Libera entrada na porta 3306 (MySQL) para qualquer IPv4

        DatabaseInstance databaseInstance = DatabaseInstance.Builder // Inicia o builder da instância de banco de dados
                .create(this, "Rds01") // Cria o recurso com o id lógico "Rds01"
                .instanceIdentifier("aws-project01-db") // Nome (identificador) da instância no RDS
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder() // Define o engine do banco como MySQL
                                .version(MysqlEngineVersion.VER_5_7) // Versão do MySQL: 5.7
                        .build())) // Finaliza as propriedades do engine MySQL
                .vpc(vpc) // VPC onde a instância será criada
                .credentials(Credentials.fromUsername("admin", // Credenciais com usuário "admin"
                        CredentialsFromUsernameOptions.builder() // Opções adicionais das credenciais
                                .password(SecretValue.unsafePlainText(databasePassword.getValueAsString())) // Senha vinda do parâmetro, em texto plano
                                .build())) // Finaliza as opções de credenciais
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // Tipo da instância: t2.micro
                .multiAz(false) // Desabilita implantação Multi-AZ
                .allocatedStorage(5) // Armazenamento alocado de 5 GB
                .securityGroups(Collections.singletonList(iSecurityGroup)) // Associa o security group à instância
                .vpcSubnets(SubnetSelection.builder() // Seleciona as subnets onde o RDS ficará
                        .subnets(vpc.getPrivateSubnets()) // Usa as subnets privadas da VPC
                        .build()) // Finaliza a seleção de subnets
                .build(); // Constrói a instância do banco de dados

        CfnOutput.Builder.create(this, "rds-endpoint") // Cria uma saída (output) para o endpoint do RDS
                .exportName("rds-endpoint") // Nome de exportação para uso em outras stacks
                .value(databaseInstance.getDbInstanceEndpointAddress()) // Valor: endereço do endpoint do banco
                .build(); // Constrói o output

        CfnOutput.Builder.create(this, "rds-password") // Cria uma saída (output) para a senha do RDS
                .exportName("rds-password") // Nome de exportação para uso em outras stacks
                .value(databasePassword.getValueAsString()) // Valor: senha informada no parâmetro
                .build(); // Constrói o output
    }
}
