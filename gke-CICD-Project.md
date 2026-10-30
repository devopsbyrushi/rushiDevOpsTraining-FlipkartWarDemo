# RushiInfotech – Flipkart CI/CD with Jenkins, Docker Hub and GKE

## End-to-End DevOps Project

**Workflow:** GitHub → Jenkins → Maven → Docker → Docker Hub → GKE → LoadBalancer → Browser

---

# 1. Project Configuration

| Component | Configuration |
|---|---|
| GCP Project | august2027 |
| GKE Cluster | rushi-gke |
| GKE Zone | us-central1-a |
| Number of Nodes | 2 |
| Machine Type | e2-medium |
| GitHub Repository | https://github.com/devopsbyrushi/rushiDevOpsTraining-FlipkartWarDemo.git |
| Jenkins Git Credential | rushi-github-credentials |
| Docker Hub Username | devopsbyrushi |
| Docker Image | devopsbyrushi/octsecurebankcicdpipleline:v1 |
| Docker Hub Credential ID | dockerhub-passwd |
| GCP Service Account Credential ID | GKE-SA |
| Kubernetes Deployment | flipkart-app |
| Kubernetes Service | flipkart-service |
| Container Port | 8080 |
| Service Port | 80 |
| Domain | rushilabs.in |

**Note:** Use the exact credential IDs configured in Jenkins. If your GCP JSON credential is saved as `GKE11` instead of `GKE-SA`, replace `GKE-SA` in the Jenkinsfile.

---

# 2. Project Architecture

```text
                 GitHub Repository
                        |
                        |
                        v
                  Jenkins Server
                        |
                 GitHub Checkout
                        |
                        v
                   Maven Build
                        |
                        v
                   Docker Build
                        |
                        v
                  Docker Hub Push
                        |
                        v
                Google Cloud Login
                        |
                        v
                  Connect to GKE
                        |
                        v
                kubectl apply -f k8s/
                        |
                        v
                   GKE Cluster
                        |
                        v
                 Kubernetes Deployment
                        |
                        v
                    Two Pods
                        |
                        v
                 LoadBalancer Service
                        |
                        v
                   External IP
                        |
                        v
                    Web Browser
```

---

# 3. Create GKE Cluster

Run these commands in Google Cloud Shell.

## Step 1: Set GCP Project

```bash
gcloud config set project august2027
```

## Step 2: Enable GKE API

```bash
gcloud services enable container.googleapis.com
```

## Step 3: Create GKE Cluster

```bash
gcloud container clusters create rushi-gke \
  --project=august2027 \
  --zone=us-central1-a \
  --release-channel=regular \
  --num-nodes=2 \
  --machine-type=e2-medium \
  --disk-type=pd-balanced \
  --disk-size=30 \
  --enable-ip-alias
```

Wait until cluster creation completes.

## Step 4: Connect kubectl to GKE

```bash
gcloud container clusters get-credentials rushi-gke \
  --project=august2027 \
  --zone=us-central1-a
```

## Step 5: Verify Cluster

```bash
kubectl cluster-info
kubectl get nodes
```

Expected result: Both GKE nodes should show `Ready`.

---

# 4. Install kubeadm, kubelet and kubectl

**Important:** This section is for installing Kubernetes tools on separate Ubuntu/Debian Linux VMs for self-managed Kubernetes practice.

GKE is a managed Kubernetes service. You do not need kubeadm to deploy applications to GKE. Jenkins uses `gcloud`, `kubectl` and `gke-gcloud-auth-plugin`.

## Step 1: Update packages

```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg
```

## Step 2: Configure Kubernetes repository

```bash
sudo mkdir -p -m 755 /etc/apt/keyrings
```

```bash
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.36/deb/Release.key \
  | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
```

```bash
sudo chmod 644 /etc/apt/keyrings/kubernetes-apt-keyring.gpg
```

```bash
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.36/deb/ /' \
  | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

```bash
sudo chmod 644 /etc/apt/sources.list.d/kubernetes.list
```

## Step 3: Install Kubernetes packages

```bash
sudo apt-get update

sudo apt-get install -y kubelet kubeadm kubectl

sudo apt-mark hold kubelet kubeadm kubectl
```

## Step 4: Verify installation

```bash
kubeadm version
```

```bash
kubelet --version
```

```bash
kubectl version --client
```

**Note:** These commands install the Kubernetes tools only. They do not create a Kubernetes cluster. A self-managed cluster additionally requires a container runtime, networking, swap configuration, `kubeadm init` on the control-plane and `kubeadm join` on worker nodes.

---

# 5. Basic Kubernetes Practice – NGINX Pod

## Step 1: Create NGINX Pod

```bash
kubectl run nginx-pod --image=nginx:latest
```

## Step 2: Check Pod

```bash
kubectl get pods -o wide
```

## Step 3: Describe Pod

```bash
kubectl describe pod nginx-pod
```

## Step 4: Check Pod Logs

```bash
kubectl logs nginx-pod
```

## Step 5: Delete Pod

```bash
kubectl delete pod nginx-pod
```

```bash
kubectl get pods
```

**Key Point:** A standalone Pod created using `kubectl run` is not automatically recreated after deletion.

---

# 6. Create Kubernetes Deployment and Service Files in GitHub

Repository:

https://github.com/devopsbyrushi/rushiDevOpsTraining-FlipkartWarDemo

Create the following folder structure:

```text
rushiDevOpsTraining-FlipkartWarDemo/
│
├── src/
├── pom.xml
├── Dockerfile
│
└── k8s/
    ├── deployment.yaml
    └── service.yaml
```

## Step 1: Create deployment.yaml

File location:

`k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flipkart-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: flipkart-app
  template:
    metadata:
      labels:
        app: flipkart-app
    spec:
      containers:
        - name: flipkart-app
          image: docker.io/devopsbyrushi/octsecurebankcicdpipleline:v1
          imagePullPolicy: Always
          ports:
            - name: http
              containerPort: 8080
```

## Step 2: Create service.yaml

File location:

`k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: flipkart-service
spec:
  type: LoadBalancer
  selector:
    app: flipkart-app
  ports:
    - name: http
      port: 80
      targetPort: 8080
      protocol: TCP
```

**Important:** The Service selector `app: flipkart-app` must match the Pod labels in the Deployment.

Commit and push both YAML files to GitHub.

---

# 7. Jenkins Server Prerequisites

The Jenkins VM should have the following tools installed:

- Git
- Java
- Maven
- Docker
- Google Cloud CLI
- kubectl
- gke-gcloud-auth-plugin

## Verify installations

```bash
git --version
```

```bash
java -version
```

```bash
mvn -version
```

```bash
docker version
```

```bash
gcloud version
```

```bash
kubectl version --client
```

```bash
gke-gcloud-auth-plugin --version
```

## Install kubectl and GKE authentication plugin

For a Debian/Ubuntu Jenkins VM with the Google Cloud CLI repository configured:

```bash
sudo apt-get update

sudo apt-get install -y kubectl google-cloud-cli-gke-gcloud-auth-plugin
```

Verify:

```bash
kubectl version --client
gke-gcloud-auth-plugin --version
```

Ensure these commands are available to the Jenkins service user.

---

# 8. Configure Jenkins Credentials

Navigate to:

**Manage Jenkins → Credentials → System → Global Credentials**

Create or verify the following credentials.

| Credential ID | Type | Purpose |
|---|---|---|
| rushi-github-credentials | Username with password / Git credential | GitHub checkout |
| dockerhub-passwd | Secret text | Docker Hub authentication |
| GKE-SA | Secret file | GCP service account JSON |

The GCP service account must have the IAM permissions required for GKE deployment.

Never upload your JSON key or Docker Hub token to GitHub.

---

# 9. Complete Jenkins Scripted Pipeline

Navigate to:

**Jenkins → Your Pipeline Job → Configure → Pipeline → Pipeline Script**

Use the following pipeline.

```groovy
node {

    stage('GitHub Code Checkout') {

        git credentialsId: 'rushi-github-credentials',
        url: 'https://github.com/devopsbyrushi/rushiDevOpsTraining-FlipkartWarDemo.git'

    }

    stage('Maven Validate') {

        sh 'mvn validate'

    }

    stage('Maven Install') {

        sh 'mvn clean install'

    }

    // Optional: Keep this stage only if separate Tomcat deployment is required.

    stage('Copy Artifact to Tomcat Webapps') {

        sh 'sudo cp target/flipkart-clone.war /opt/tomcat10/webapps/'

    }

    stage('Build Docker Image') {

        sh 'docker build -t devopsbyrushi/octsecurebankcicdpipleline:v1 .'

    }

    stage('Docker Hub Login') {

        withCredentials([string(
            credentialsId: 'dockerhub-passwd',
            variable: 'rushidockerhubpassword'
        )]) {

            sh '''
                echo "$rushidockerhubpassword" |
                docker login -u devopsbyrushi --password-stdin
            '''

        }

    }

    stage('Push Image to Docker Hub') {

        sh 'docker push devopsbyrushi/octsecurebankcicdpipleline:v1'

    }

    stage('Deploying to GKE') {

        withCredentials([file(
            credentialsId: 'GKE-SA',
            variable: 'GKE_SA'
        )]) {

            sh '''

                set -e

                echo "Authenticating with Google Cloud..."

                gcloud auth activate-service-account \
                    --key-file="$GKE_SA" \
                    --project=august2027

                gcloud config set project august2027

                echo "Connecting to GKE..."

                gcloud container clusters get-credentials rushi-gke \
                    --project=august2027 \
                    --zone=us-central1-a

                echo "Checking GKE Nodes..."

                kubectl get nodes

                echo "Applying Kubernetes YAML files..."

                kubectl apply -f k8s/

                echo "Checking Deployment..."

                kubectl get deployment flipkart-app

                echo "Checking Pods..."

                kubectl get pods -l app=flipkart-app

                echo "Checking Services..."

                kubectl get svc

            '''

        }

    }

    stage('Display LoadBalancer External IP') {

        withCredentials([file(
            credentialsId: 'GKE-SA',
            variable: 'GKE_SA'
        )]) {

            sh '''

                set -e

                gcloud auth activate-service-account \
                    --key-file="$GKE_SA" \
                    --project=august2027

                gcloud container clusters get-credentials rushi-gke \
                    --project=august2027 \
                    --zone=us-central1-a

                echo "GKE Services:"

                kubectl get svc

                EXTERNAL_IP=$(kubectl get svc flipkart-service \
                    -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

                if [ -n "$EXTERNAL_IP" ]; then

                    echo "Flipkart Application URL: http://$EXTERNAL_IP"

                else

                    echo "LoadBalancer External IP is still pending."

                fi

            '''

        }

    }

}
```

**Pipeline Notes:**

1. GitHub checkout downloads the application source and Kubernetes YAML files.
2. Maven validates and builds the application.
3. Docker builds the application image.
4. Jenkins logs in to Docker Hub using stored credentials.
5. Jenkins pushes the Docker image.
6. Jenkins authenticates to Google Cloud.
7. Jenkins connects to the GKE cluster.
8. Jenkins applies the YAML files from the GitHub repository.
9. Jenkins displays the LoadBalancer External IP.

The Tomcat copy stage is optional. Remove it if you are deploying only through Docker and GKE. Confirm the Tomcat path and Jenkins permissions if you retain it.

---

# 10. Verify Deployment from Cloud Shell

Connect to the cluster:

```bash
gcloud config set project august2027

gcloud container clusters get-credentials rushi-gke \
  --project=august2027 \
  --zone=us-central1-a
```

## Check Nodes

```bash
kubectl get nodes
```

## Check Deployment

```bash
kubectl get deployments
```

## Check Pods

```bash
kubectl get pods -o wide
```

## Check Deployment Status

```bash
kubectl rollout status deployment/flipkart-app
```

## Check Service

```bash
kubectl get svc flipkart-service
```

## Check Endpoints

```bash
kubectl get endpoints flipkart-service
```

## Get only External IP

```bash
kubectl get svc flipkart-service \
  -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

Open the application:

```text
http://EXTERNAL_IP
```

**Important:** The LoadBalancer listens on port 80 and forwards traffic to container port 8080. Do not add `:8080` to the browser URL.

---

# 11. Demonstrate Pod Self-Healing

List the application Pods:

```bash
kubectl get pods -l app=flipkart-app
```

Delete one Pod using its actual name:

```bash
kubectl delete pod POD_NAME
```

Watch Kubernetes create a replacement:

```bash
kubectl get pods -l app=flipkart-app --watch
```

Press `Ctrl+C` after observing the replacement.

**Key Point:** The Deployment maintains the desired replica count and creates replacement Pods when a Pod is deleted.

Optional: Delete all application Pods:

```bash
kubectl delete pods -l app=flipkart-app
```

Watch the replacement Pods:

```bash
kubectl get pods -l app=flipkart-app --watch
```

---

# 12. Scale Kubernetes Deployment

## Scale to 4 replicas

```bash
kubectl scale deployment flipkart-app --replicas=4
```

```bash
kubectl get pods -l app=flipkart-app
```

## Scale back to 2 replicas

```bash
kubectl scale deployment flipkart-app --replicas=2
```

```bash
kubectl get pods -l app=flipkart-app
```

---

# 13. Update Application Code and Deploy Changes

When source code changes, for example, changing a product price from ₹40,000 to ₹45,000, the running Pods do not automatically receive the new application files.

Follow this sequence:

1. Update the source code.
2. Commit and push changes to GitHub.
3. Run the Jenkins pipeline.
4. Confirm Maven creates a new WAR file.
5. Confirm Docker builds the updated image.
6. Push the updated image to Docker Hub.
7. Deploy the updated image to GKE.
8. Verify the new Pods.
9. Refresh the application in the browser.

**Important Image Tag Note:**

Reusing `v1` makes it difficult to identify which application version is running.

Recommended approach: Use a unique image tag for every Jenkins build, such as:

```text
devopsbyrushi/octsecurebankcicdpipleline:BUILD_NUMBER
```

Use the same tag when building, pushing and deploying.

If you continue using `v1`, your Deployment uses:

```yaml
imagePullPolicy: Always
```

However, a new Pod must be created to pull the updated image. `imagePullPolicy: Always` does not automatically restart existing Pods.

Useful commands:

```bash
kubectl describe deployment flipkart-app
```

```bash
kubectl get pods -l app=flipkart-app -o wide
```

```bash
kubectl rollout history deployment/flipkart-app
```

---

# 14. Configure GoDaddy DNS

Domain:

```text
rushilabs.in
```

Navigate to:

**GoDaddy → Domain → DNS Management**

Edit the existing parked root A record. Do not create a duplicate root A record.

Configure:

| Type | Name | Value | TTL |
|---|---|---|---|
| A | @ | GKE LoadBalancer External IP | 600 seconds |
| CNAME | www | rushilabs.in | Existing record |

Get the current LoadBalancer IP:

```bash
kubectl get svc flipkart-service
```

Copy the IP shown under `EXTERNAL-IP` and enter it in the GoDaddy A record.

An earlier lab screenshot showed:

```text
34.56.229.191
```

Verify that this is still the current IP before using it.

After DNS propagation, test:

```text
http://rushilabs.in
```

```text
http://www.rushilabs.in
```

**Important:** DNS only points the domain to the LoadBalancer. HTTPS/SSL must be configured separately.

---

# 15. Troubleshooting

## kubectl: command not found

Install kubectl on the Jenkins VM and ensure it is available to the Jenkins service user.

## gke-gcloud-auth-plugin missing

Install the GKE authentication plugin:

```bash
sudo apt-get install -y google-cloud-cli-gke-gcloud-auth-plugin
```

Verify:

```bash
gke-gcloud-auth-plugin --version
```

## 403 Insufficient Authentication Scopes

If using the VM's attached service account, check VM access scopes and IAM roles.

If using the GKE-SA JSON key, check the IAM roles assigned to that service account.

These are two different authentication methods.

## Cloud Resource Manager API disabled

Run using an administrator account:

```bash
gcloud services enable cloudresourcemanager.googleapis.com \
  --project=august2027
```

## GKE Connection Timeout

Error example:

```text
dial tcp IP_ADDRESS:443: i/o timeout
```

Check:

- GKE endpoint configuration.
- Private or public cluster settings.
- Master authorized networks.
- Firewall rules.
- Network connectivity from Jenkins to the GKE control plane.

Successful authentication does not guarantee network connectivity.

## LoadBalancer External IP Pending

Wait a few minutes and run:

```bash
kubectl get svc flipkart-service
```

## Updated Application Changes Not Visible

Check:

1. Latest GitHub commit.
2. Maven WAR build.
3. Docker image build.
4. Docker Hub push.
5. Kubernetes Deployment image.
6. Running Pods.
7. Browser cache.

---

# 16. Delete Kubernetes Application Resources

Run these commands from Cloud Shell while connected to the cluster.

## Delete LoadBalancer Service

```bash
kubectl delete service flipkart-service
```

## Delete Application Deployment

```bash
kubectl delete deployment flipkart-app
```

## Delete NGINX Pod

```bash
kubectl delete pod nginx-pod --ignore-not-found
```

Verify:

```bash
kubectl get all
```

---

# 17. Delete GKE Cluster – Final Cleanup

**WARNING:** Deleting the GKE cluster permanently removes the cluster and its managed workloads.

Perform this step only after completing the lab.

## Step 1: Delete GKE Cluster

```bash
gcloud container clusters delete rushi-gke \
  --project=august2027 \
  --zone=us-central1-a
```

Enter `Y` when prompted for confirmation.

## Step 2: Verify Cluster Deletion

```bash
gcloud container clusters list --project=august2027
```

The `rushi-gke` cluster should no longer appear.

## Step 3: Check Remaining Disks

```bash
gcloud compute disks list --project=august2027
```

## Step 4: Check Static IP Addresses

```bash
gcloud compute addresses list --project=august2027
```

Review remaining resources before deleting them.

**Important:** Deleting the GKE cluster does not delete:

- Jenkins VM
- GitHub repository
- Docker Hub image
- GoDaddy domain
- Other independent Google Cloud resources

Delete other resources separately only if they are no longer required.

---

# 18. Final End-to-End Checklist

- [ ] GKE API enabled.
- [ ] GKE cluster created.
- [ ] Both GKE nodes show Ready.
- [ ] NGINX Pod created.
- [ ] NGINX Pod inspected and deleted.
- [ ] Kubernetes deployment.yaml created in GitHub.
- [ ] Kubernetes service.yaml created in GitHub.
- [ ] Jenkins GitHub checkout successful.
- [ ] Maven validate successful.
- [ ] Maven install successful.
- [ ] Docker image built successfully.
- [ ] Docker Hub login successful.
- [ ] Docker image pushed successfully.
- [ ] Jenkins authenticated to Google Cloud.
- [ ] Jenkins connected to GKE.
- [ ] kubectl get nodes successful.
- [ ] kubectl apply -f k8s/ successful.
- [ ] Application Pods running.
- [ ] LoadBalancer Service created.
- [ ] External IP assigned.
- [ ] Application accessible through External IP.
- [ ] GoDaddy DNS configured.
- [ ] Domain resolves to the LoadBalancer.
- [ ] Pod self-healing demonstrated.
- [ ] Deployment scaling demonstrated.
- [ ] GKE cluster deleted after lab completion.

---

# END OF RUSHIINFOTECH FLIPKART CI/CD + GKE LAB

**Technologies Covered:**

GitHub | Jenkins | Maven | Docker | Docker Hub | Google Cloud | GKE | Kubernetes | kubectl | kubeadm | LoadBalancer | DNS
