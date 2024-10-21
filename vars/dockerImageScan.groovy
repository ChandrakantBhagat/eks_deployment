// def call(String project, String ImageTag, String hubUser){
    
//     sh """   
//      trivy image ${hubUser}/${project}:latest > scan.txt
//      cat scan.txt
//     """
// }

def call(String aws_account_id, String region, String ECR_REPO_NAME){
    
    sh """
    trivy image ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${ECR_REPO_NAME}:${BUILD_NUMBER} > scan.txt
    cat scan.txt
    """
}   
