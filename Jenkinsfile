@Library('my_shared_lib') _

pipeline {
    agent { 
        label 'qa'
    }

    parameters {
        choice(name: 'action', choices: 'create\ndelete', description: 'choose create/Destroy')
        string(name: 'aws_account_id', description: 'AWS account ID', defaultValue: '390403884474') 
        string(name: 'region', description: 'region for ECR', defaultValue: 'us-east-1')
        string(name: 'ECR_REPO_NAME', description: 'Name of the ECR', defaultValue: 'devpractice')
        string(name: 'cluster', description: 'Name of the EKS cluster', defaultValue: 'my-eks-cluster')
    }

    environment {
        AWS_REGION = "${params.region}"
        AWS_ACCESS_KEY_ID = credentials('6ec2def6-6907-4fec-b555-c645ede51725')
        AWS_SECRET_ACCESS_KEY = credentials('6ec2def6-6907-4fec-b555-c645ede51725')
        KUBECTL_VERSION = 'v1.31.1'
    } 

    stages {
        stage('Assume Role') {
            steps {
                script {
                    def roleArn = "arn:aws:iam::390403884474:role/devops_multiaccount"
                    def assumeRoleOutput = sh(script: "aws sts assume-role --role-arn ${roleArn} --role-session-name jenkins-deploy", returnStdout: true)
                    def json = readJSON(text: assumeRoleOutput)
                    env.AWS_ACCESS_KEY_ID = json.Credentials.AccessKeyId
                    env.AWS_SECRET_ACCESS_KEY = json.Credentials.SecretAccessKey
                    env.AWS_SESSION_TOKEN = json.Credentials.SessionToken
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/kailas135/eks_deployment.git'
            }
        }

        stage('Unit Test Maven') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    mvnTest()
                }
            }
        }

        stage('Integration Test Maven') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {                   
                    mvnIntegrationTest()
                }
            }
        }

        stage('Static Code Analysis: SonarQube') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    def sonarQubeCredentialsId = 'sonar-api'
                    statiCodeAnalysis(sonarQubeCredentialsId)
                }
            }
        }

        stage('Maven Build') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    mvnBuild()
                }
            }
        }

        stage('Docker Image Build: ECR') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    dockerBuild("${params.aws_account_id}", "${params.region}", "${params.ECR_REPO_NAME}")
                }
            }
        }

        stage('Docker Image Scan: ECR') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    dockerImageScan("${params.aws_account_id}", "${params.region}", "${params.ECR_REPO_NAME}") 
                }
            }
        }

        stage('Docker Image Push: ECR') {
            when { 
                expression { params.action == 'create' } 
            }
            steps {
                script {
                    dockerImagePush("${params.aws_account_id}", "${params.region}", "${params.ECR_REPO_NAME}")
                    sh """
                        sed -i "s|${params.aws_account_id}.dkr.ecr.${params.region}.amazonaws.com/${params.ECR_REPO_NAME}:[0-9]*|${params.aws_account_id}.dkr.ecr.${params.region}.amazonaws.com/${params.ECR_REPO_NAME}:${BUILD_NUMBER}|g" deployment.yaml
                    """
                }
            }
        }

        stage('Deployment on EKS Cluster') {
            when { expression { params.action == 'create' } }
            steps {
                script {
                    sh """
                        aws eks --region ${params.region} update-kubeconfig --name ${params.cluster}
                        curl -LO "https://dl.k8s.io/release/${env.KUBECTL_VERSION}/bin/linux/amd64/kubectl"
                        chmod +x kubectl
                        sudo mv kubectl /usr/local/bin/
                        ls -alh  # List files to check for service.yaml
                        kubectl apply -f deployment.yaml -f service.yaml
                    """                    
                }
            }
        }

        // stage('Docker Image Cleanup: ECR') {
        //     when { 
        //         expression { params.action == 'create' } 
        //     }
        //     steps {
        //         script {
        //             dockerImageCleanup("${params.aws_account_id}", "${params.region}", "${params.ECR_REPO_NAME}")
        //         }
        //     }
        // }

    }
}
