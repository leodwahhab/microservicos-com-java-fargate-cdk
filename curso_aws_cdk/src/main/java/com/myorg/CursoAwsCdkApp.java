package com.myorg;

import software.amazon.awscdk.App;

public class CursoAwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpcStack = new VpcStack(app, "Vpc");

        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addStackDependency(vpcStack);

        RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addStackDependency(vpcStack);

        SnsStack snsStack = new SnsStack(app, "SnsStack");

        Service01Stack service01Stack = new Service01Stack(app, "Service01", clusterStack.getCluster());
        service01Stack.addStackDependency(clusterStack);
        service01Stack.addStackDependency(rdsStack);
        service01Stack.addStackDependency(snsStack);

        app.synth();
    }
}

