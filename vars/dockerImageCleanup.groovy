
def call(String aws_account_id, String region, String ecr_repoName){
    
    sh """
     docker rmi ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${ecr_repoName}:${BUILD_NUMBER}
    
    """
}

//aws ecr batch-delete-image --repository-name ${ecr_repoName} --image-ids imageTag=${BUILD_NUMBER} --region ${region}