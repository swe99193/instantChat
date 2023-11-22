# instantChat

## Demo
message pagination
![message pagination](/docs/images/InstantChat-pagination.gif)
TBD...


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

Build React frontend
```
cd frontend
npm run build
```

Build Docker image for each microservices (Java)
```
cd account-service
./gradlew build
docker build --tag swe99193/account-service .
docker push swe99193/account-service 
```
```
cd chat-service
./gradlew build
docker build --tag swe99193/chat-service .
docker push swe99193/chat-service 
```
```
cd conversation-service
./gradlew build
docker build --tag swe99193/conversation-service .
docker push swe99193/conversation-service 
```
```
cd file-upload-service
./gradlew build
docker build --tag swe99193/file-upload-service .
docker push swe99193/file-upload-service 
```
```
cd message-service
./gradlew build
docker build --tag swe99193/message-service .
docker push swe99193/message-service 
```

## Local development setup

Expected results:
* Access frontend at `yourchatdomain.com:3000`
* Access backend at `yourchatdomain.com` (allow unsafe browsing)

Requirements:
* minikube
* kubectl


Open React server
```
npm start
```
Setup a local minikube cluster
```
minikube start
minikube addons enable ingress
minikube tunnel
```
(optional) Open minikube dashboard
```
minikube dashboard
```
Add host mapping in `/etc/hosts`
```
127.0.0.1       yourchatdomain.com
```
Configure CORS in `k8/Secrets.yaml`
```
FRONTEND_URL=https://yourchatdomain.com:3000
```
Configure your secrets (refer to `Secrets-sample.yaml`) and apply kubernetes configuration  
```
kubectl apply -f k8/Microservice.yaml
kubectl apply -f k8/Secrets.yaml
```
delete minikube cluster
```
minikube delete
```