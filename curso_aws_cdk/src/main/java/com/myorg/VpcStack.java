package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {
    private Vpc vpc;

    public VpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "Vpc01")
                .maxAzs(2) // quantidade de zonas de disponibilidade
                .natGateways(0) // define 0 natGatways. feito para minimizar custos durante o curso, mas não é aconselhado
                                // fazê-lo pois com as alterações feitas ao llongo do curso, poderá expor as instancias
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
