pipeline {
    agent any

    environment {
        GIT_URL = 'https://github.com/SKALA-T1F5/SKIB-BE.git'
        GIT_BRANCH = 'main'
        GIT_ID = 'skala-github-yoonali'
        GIT_USER_NAME = 'yoonali'
        GIT_USER_EMAIL = 'yoonalim2003@gmail.com'
        IMAGE_REGISTRY = 'docker.io/yoonalim2003'
        IMAGE_NAME = 'sk-team-09-backend'
        IMAGE_TAG = '1.0.0'
        DOCKER_CREDENTIAL_ID = 'docker-hub-credentials-id'
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: "${GIT_BRANCH}",
                    url: "${GIT_URL}",
                    credentialsId: "${GIT_ID}"   // GitHub PAT credential ID
            }
        }

        stage('Build with Gradle') {
            steps {
                sh './gradlew clean build -x test'
            }
        }


        stage('Docker Build & Push') {
            steps {
                script {
                    // 해시코드 12자리 생성
                    def hashcode = sh(
                        script: "date +%s%N | sha256sum | cut -c1-12",
                        returnStdout: true
                    ).trim()

                    // Build Number + Hash Code 조합 (IMAGE_TAG는 유지)
                    def FINAL_IMAGE_TAG = "${IMAGE_TAG}-${BUILD_NUMBER}-${hashcode}"
                    echo "Final Image Tag: ${FINAL_IMAGE_TAG}"

                    def registryUrl = IMAGE_REGISTRY.split('/')[0]
                    docker.withRegistry("https://${registryUrl}", "${DOCKER_CREDENTIAL_ID}") {
                        def appImage = docker.build("${IMAGE_REGISTRY}/${IMAGE_NAME}:${FINAL_IMAGE_TAG}", "--platform linux/amd64 .")
                        appImage.push()
                    }

                    // 최종 이미지 태그를 env에 등록 (나중에 deploy.yaml 수정에 사용)
                    env.FINAL_IMAGE_TAG = FINAL_IMAGE_TAG
                }
            }
        }



        stage('Update deploy.yaml and Git Push') {
            steps {
                script {
                    def newImageLine = "          image: ${env.IMAGE_REGISTRY}/${env.IMAGE_NAME}:${env.FINAL_IMAGE_TAG}"
                    def gitRepoPath = env.GIT_URL.replaceFirst(/^https?:\/\//, '')

                    sh """
                        sed -i 's|^[[:space:]]*image:.*\$|${newImageLine}|g' ./k8s/deploy.yaml
                        cat ./k8s/deploy.yaml
                    """

                    sh """
                        git config user.name "$GIT_USER_NAME"
                        git config user.email "$GIT_USER_EMAIL"
                        git add ./k8s/deploy.yaml || true
                    """

                    withCredentials([usernamePassword(credentialsId: "${env.GIT_ID}", usernameVariable: 'GIT_PUSH_USER', passwordVariable: 'GIT_PUSH_PASSWORD')]) {
                        sh """
                            if ! git diff --cached --quiet; then
                                git commit -m "[AUTO] Update deploy.yaml with image ${env.FINAL_IMAGE_TAG}"
                                git remote set-url origin https://${GIT_PUSH_USER}:${GIT_PUSH_PASSWORD}@${gitRepoPath}
                                git push origin ${env.GIT_BRANCH}
                            else
                                echo "No changes to commit."
                            fi
                        """
                    }
                }
            }
        }

    }
}