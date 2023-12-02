# InstantChat

A chat application based on a microservice architecture and deployed with Kubernetes (AWS EKS). 

## Features
### Available
* Message pagination
* Real-time and multiple-device update of read flag
* Real-time and multiple-device update of unread count (not 100% synchornized if network goes offline)
* Websocket client auto reconnection upon disconnection or network failure
* File uploads such as pdf, jpg, zip, etc.
* Image display as dialog
* Open new conversations with existing users

### Possible extensions
* Support online status
* Support conversation pagination
* Route Kafka pub/sub messsages to websocket servers that a receiver is connected to
* Support video display


## Demo
### basic layout
(screenshot)

### message pagination
![InstantChat-pagination.gif](/docs/images/InstantChat-pagination.gif)

### send/recieve messages
(screenshot)

### send/recieve images and files
(screenshot)

### read flag & unread count
(screenshot)

### multi-device updates
(screenshot)

### start a new conversation
(screenshot)

### login
(screenshot)

### misc (set status, profile pic, password etc)
(screenshot)


## Structure
### Microservices
* `account-service`: account registration, recovery, login, logout, profile-picture, metadata
* `chat-service`: manage websockets connection, consume Kafka events and route to websockets
* `conversation-service`: create/list conversations, handle new message & read event of conversations
* `file-upload-service`: file upload
* `message-service`: save/list messages, generate S3 presigned url, query unread counts
### Infrastructure
* Redis: login session
* Kafka: pub/sub system
* PostgreSQL: storage for user accounts, user metadata, conversation metadata
* DynamoDB (AWS): storage for messages
* S3 (AWS): storage for images, files, profile pictures


## Build Project
Requirements:
* Node v16.16.0
* Java 17 (recommended)

### Frontend
Build React frontend
```
cd frontend
npm run build
```

### Backend
Build Docker image for each microservices (Java)
```
cd account-service
./gradlew build
docker build --platform=linux/amd64 --tag swe99193/account-service .
docker push swe99193/account-service 
```
```
cd chat-service
./gradlew build
docker build --platform=linux/amd64 --tag swe99193/chat-service .
docker push swe99193/chat-service 
```
```
cd conversation-service
./gradlew build
docker build --platform=linux/amd64 --tag swe99193/conversation-service .
docker push swe99193/conversation-service 
```
```
cd file-upload-service
./gradlew build
docker build --platform=linux/amd64 --tag swe99193/file-upload-service .
docker push swe99193/file-upload-service 
```
```
cd message-service
./gradlew build
docker build --platform=linux/amd64 --tag swe99193/message-service .
docker push swe99193/message-service 
```


## Cloud setup
Expected results:
* Access frontend at `https://d1kqmv9mmy5u7j.cloudfront.net` (example)
* Access backend at `http://k8s-default-instantc-7d687aa992-1714336368.us-east-1.elb.amazonaws.com/` (example)


### Frontend
Requirements:
* AWS S3 
* AWS CloudFront 

#### Host a static website with https
1. After building frontend, upload files under `/frontend/build` to S3 bucket and point a CloudFront distribution to the bucket.  
    Refer to [How do I use CloudFront to serve HTTPS requests for my Amazon S3 bucket?](https://us-east-1.console.aws.amazon.com/cloudfront/v4/home?region=us-east-1#/distributions/E2Y765QAACISBQ)

    ![CloudFront-config.png](/docs/images/CloudFront-config.png)

2. Define default root object as index.html
    ![CloudFront-default-root-object.png](/docs/images/CloudFront-default-root-object.png)


3. Update permissions in S3.   
    Refer to [Giving the origin access control permission to access the S3 bucket](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html#oac-permission-to-access-s3)

4. Create custom error response to direct all paths to `index.html`. 
    ![CloudFront-custtom-error-response.png](/docs/images/CloudFront-custtom-error-response.png)


### Backend

Requirements:
* kubectl
* eksctl
* AWS EKS


#### Setup
Create a new EKS cluster using public subnets in the default VPC
```
eksctl create cluster --name your-cluster --region us-east-1 --vpc-public-subnets subnet-0d036e08952b71b71,subnet-04bc8402e4396ead8
```

❗️Check if these add-ons are installed: Amazon VPC CNI, CoreDNS, kube-proxy

Create an IAM OIDC provider for your cluster (required once)  
https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html

Create an AWS Load Balancer Controller  
https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html

Verify that AWS Load Balancer Controller is installed.
```
kubectl get deployment -n kube-system aws-load-balancer-controller
```

Add tags to the public subnets used by the cluster for LB to recognize subnets.  
Refer to the **Prerequisites** section in [Application load balancing on Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/alb-ingress.html)
```
kubernetes.io/role/elb          1
```

Configure TLS certificates to allow https connection (`Ingress`)  
ref: https://repost.aws/knowledge-center/eks-apps-tls-to-activate-https

Tips: Create a self-signed certificate with the domain of ELB (e.g., *k8s-default-instantc-xxxxxxx-xxxxxxx.us-east-1.elb.amazonaws.com*) and import to AWS Certificate Manager. 
![Self-Signed-Certificates.png](/docs/images/Self-Signed-Certificates.png)


#### Create resources

Create your secrets `k8/Secrets.yaml` (refer to `Secrets-sample.yaml`) 
```
FRONTEND_URL=https://d1kqmv9mmy5u7j.cloudfront.net    # configure CORS
```

Create the resources with yaml files
```
kubectl apply -f k8/Secrets.yaml
kubectl apply -f k8/Microservices-EKS.yaml
```

Check the created Ingress
```
kubectl describe ingress
```
![Ingress.png](/docs/images/Ingress.png)


#### Clean up
Delete EKS cluster
```
eksctl delete cluster --name your-cluster --region us-east-1
```


## Local development setup
Expected results:
* Access frontend at `https://yourchatdomain.com:3000`
* Access backend at `https://yourchatdomain.com`


### Frontend
Requirements:
* Node

Install packages and open React server
```
npm install
npm start
```


### Backend
Requirements:
* minikube
* kubectl

#### Setup
Setup a local minikube cluster
```
minikube start
minikube addons enable ingress
```

Open external connection to minikube cluster
```
minikube tunnel
```

(optional) Open minikube dashboard
```
minikube dashboard
```

Add hostname mapping in `/etc/hosts` on your local machine
```
127.0.0.1       yourchatdomain.com
```

#### Create resources

Create your secrets `k8/Secrets.yaml` (refer to `Secrets-sample.yaml`) 
```
FRONTEND_URL=https://yourchatdomain.com:3000    # configure CORS
```

Apply kubernetes configuration to create the resources
```
kubectl apply -f k8/Secrets.yaml
kubectl apply -f k8/Microservices-minikube.yaml
```

#### Clean up
Delete minikube cluster
```
minikube delete
```


## Infrastructure setup on AWS (Sample)
### Redis
1. Create a Redis cluster **in the same VPC** of your EKS cluster

2. Find the connection endpoint at Configuration Endpoint. Set the value in the `Secrets.yaml`.  
    https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/Endpoints.html
    
    ![Redis-cluster-config.png](/docs/images/Redis-cluster-config.png)

3. Configure **Security Group** of the redis cluster to allow EKS nodes to connect to the redis cluster

    Refer to [Authorize access to the cluster](https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/GettingStarted.AuthorizeAccess.html)

4. (optional) Set up connection to Redis cluster outside VPC. Only required for local development  
    https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/accessing-elasticache.html#access-from-outside-aws


### Kafka
1. Create a Kafka cluster **in the same VPC** of your EKS cluster.  

2. Configure **Security Group** of the Kafka cluster to allow EKS nodes to connect to the Kafka cluster. For example, allow inbound traffic from the security group of EKS nodes. 

3. Find Kafka bootstrap servers. Set the value in the `Secrets.yaml`.

   ![kafka-cluster-config.png](/docs/images/kafka-cluster-config.png)


### PostgreSQL
1. Set up with public ip
2. Find connection endpoint. Set the value in the `Secrets.yaml`.


### DynamoDB
1. Set partition key and sort key
![message pagination](/docs/images/DynamoDB-config.png)


### S3
1. Open a S3 bucket. Set the bucket name in the `Secrets.yaml`.
2. Upload default profile pictures in `s3/profile-picture/default/` to S3 bucket under `/profile-picture/default/`
3. Add CORS configuration to your bucket to allow presigned url. For example:
```
[
    {
        "AllowedHeaders": [
            "*"
        ],
        "AllowedMethods": [
            "GET",
        ],
        "AllowedOrigins": [
            "*"
        ],
        "ExposeHeaders": []
    }
]
```


## Misc
### useful commands
Retrieve EKS cluster context back to kubectl in case it somehow is gone. 
```
aws eks update-kubeconfig --region us-east-1 --name your-cluster
```
Inspect logs
```
kubectl logs -f deployments/chat-deployment --tail=100
```
Restart deployment
```
kubectl rollout restart deployment
kubectl rollout restart deployment account-deployment
```