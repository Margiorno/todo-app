# Todo & Collaboration Platform Backend

This is a robust backend service for a collaborative task management and social platform, built with the Spring Boot framework. It features a comprehensive RESTful API, real-time communication via WebSockets, and a secure, stateless authentication system using JWT. The project is structured as a multi-module monolith to ensure clean architecture and separation of concerns.

## Key Features

*   **Team Management**: Create teams, manage members, and generate single-use invite codes for seamless collaboration.
*   **Advanced Task Management**: Create, update, assign, and filter tasks by priority, status, date, or associated team.
*   **Real-Time Chat**: One-on-one and group conversations powered by **STOMP over WebSockets**, providing instant messaging capabilities.
*   **Social Networking**: A complete friend system with invitations, friend lists, and user status checks.
*   **Real-Time Notifications**: Users receive instant notifications for critical events like new friend requests.
*   **Secure by Design**: Endpoints are secured using Spring Security, with JWT-based authentication for a stateless and scalable architecture.
*   **OpenAPI 3.1 Documentation**: A complete and interactive API documentation is automatically generated and available via Swagger UI.
*   **File Storage**: Functionality for uploading and retrieving user-specific files, such as profile avatars.

## Tech Stack

| Category           | Technology                                                                                                  |
| ------------------ | ----------------------------------------------------------------------------------------------------------- |
| **Framework**      | Spring Boot                                                                                            |
| **Data Persistence** | Spring Data JPA, Hibernate                                                                                  |
| **Database**       | H2 In-Memory Database (for development & testing)                                                           |
| **Security**       | Spring Security, JSON Web Tokens (JWT)                                                                      |
| **Real-Time Comm** | Spring WebSocket, STOMP                                                                            |
| **API**            | RESTful API, OpenAPI 3.1 (`springdoc-openapi`)                                                              |
| **Validation**     | Spring Boot Starter Validation (`jakarta.validation`)                                                       |
| **Utilities**      | Lombok                                                                                                      |
| **Testing**        | JUnit 5, Mockito, Integration Testing with `SpringBootTest`                                                 |

## Security Architecture

The application's security is managed by Spring Security and is configured to be completely stateless.

*   **Authentication**: Handled via JWT. A custom `JwtCookieAuthenticationFilter` intercepts requests to validate a token sent in a browser cookie.
*   **Authorization**: Endpoints are split into two main `SecurityFilterChain`s:
    1.  **Public Endpoints**: (`/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, static assets) are publicly accessible.
    2.  **Protected Endpoints**: All other endpoints require successful JWT authentication.
*   **Error Handling**: A custom `AuthEntryPoint` is configured to handle authentication failures gracefully.
*   **WebSocket Security**: WebSocket connections are secured through the main Spring Security context established during the HTTP handshake.

## Real-Time Functionality (WebSockets)

Real-time features like live chat and notifications are implemented using **STOMP over WebSockets** to enable a full-duplex, event-driven communication channel.

*   **Endpoint**: The primary STOMP handshake endpoint is available at `/ws`.
*   **Message Broker**: A simple in-memory message broker is configured to handle message routing.
    *   **Application Prefix**: `/app` - Messages sent from clients to this destination are routed to `@MessageMapping` methods in controllers (e.g., `ChatWebSocketController`).
    *   **Broker Prefixes**: `/topic`, `/queue` - For broadcasting messages (pub-sub) and private messages.
    *   **User Destination**: `/user` - Enables sending messages directly to a specific user's queue.

## Getting Started

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Margiorno/todo-app.git
    cd todo-app
    ```

2.  **Run the application:**
    // TODO

3.  The application will start on port **11111**.
    *   **API URL**: `http://localhost:11111`
    *   **H2 Console**: `http://localhost:11111/h2-console` (User: `admin`, Pass: `password`)

## API Documentation

The API is documented using the OpenAPI 3.1 specification. Once the application is running, you can access the interactive Swagger UI to explore and test the endpoints.

*   **Swagger UI URL**: [http://localhost:11111/swagger-ui.html](http://localhost:11111/swagger-ui.html)
*   **OpenAPI Spec URL**: [http://localhost:11111/v3/api-docs](http://localhost:11111/v3/api-docs)

### API Endpoint Overview

The API is logically grouped by functionality:

| API Group           | Description                                                                 | Example Endpoints                                              |
| ------------------- | --------------------------------------------------------------------------- | -------------------------------------------------------------- |
| **Teams API**       | Creating and managing teams and their members.                              | `POST /teams/create`, `POST /teams/join`, `GET /teams/{id}/members` |
| **Task API**        | Managing user and team tasks.                                               | `POST /task/new`, `PUT /task/update/{id}`, `GET /task`          |
| **Chat API**        | Endpoints for managing chat conversations and fetching messages.            | `GET /chat/conversations`, `GET /chat/{chatId}/messages`         |
| **Social API**      | Managing friend requests and social interactions.                           | `POST /social/{receiverId}/invite`, `GET /social/friends`     |
| **User Profile API**| Managing user profiles and avatars.                                         | `GET /users/{userId}`, `PATCH /users/profile/update`            |
| **Notifications API** | Retrieving user notifications.                                              | `GET /notifications/getAll`                                    |
| **File API**        | Retrieving stored files like profile pictures.                              | `GET /files/profile-pictures/{filename}`                       |
