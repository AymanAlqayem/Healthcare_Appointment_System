# Clone the repository
git clone <repository-url>
cd Healthcare_Appointment_System

# Build the JAR file first
mvn clean package

# Start all services with Docker Compose
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d

# Stop the services
docker-compose down

# View logs
docker-compose logs -f


# Clone the repository
git clone <repository-url>
cd Healthcare_Appointment_System

# Build the JAR file first
mvn clean package

# Start all services with Docker Compose
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d

# Stop the services
docker-compose down

# View logs
docker-compose logs -f
