# Starter Templates

This directory contains optional starter code to help you get started quickly. **You are NOT required to use these templates** - feel free to structure your project however you prefer.

## What's Included

### Backend (Spring Boot)
- `backend-pom.xml` - Maven configuration with all necessary dependencies
- `application.properties` - Database and server configuration
- `Application.java` - Main Spring Boot application class
- `WebConfig.java` - CORS configuration for frontend communication

### Frontend (React + TypeScript)
- `package.json` - React app with Redux Toolkit and necessary dependencies
- `store.ts` - Example Redux store setup
- `api-client.ts` - Axios-based API client with example endpoints
- `types.ts` - TypeScript interfaces for domain models

## How to Use

### Backend Setup

1. Create your backend directory:
   ```bash
   mkdir backend
   cd backend
   ```

2. Copy the Maven configuration:
   ```bash
   cp ../starter-templates/backend-pom.xml ./pom.xml
   ```

3. Create the source structure:
   ```bash
   mkdir -p src/main/java/com/maplewood
   mkdir -p src/main/resources
   ```

4. Copy the configuration files:
   ```bash
   cp ../starter-templates/application.properties src/main/resources/
   cp ../starter-templates/Application.java src/main/java/com/maplewood/
   mkdir -p src/main/java/com/maplewood/config
   cp ../starter-templates/WebConfig.java src/main/java/com/maplewood/config/
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

1. Create React app with TypeScript:
   ```bash
   npx create-react-app frontend --template typescript
   cd frontend
   ```

2. Replace package.json with the starter template:
   ```bash
   cp ../starter-templates/package.json ./package.json
   npm install
   ```

3. Copy the starter files:
   ```bash
   mkdir -p src/store src/api src/types
   cp ../starter-templates/store.ts src/store/
   cp ../starter-templates/api-client.ts src/api/
   cp ../starter-templates/types.ts src/types/
   ```

4. Update `src/index.tsx` to include the Redux Provider (if using Redux):
   ```typescript
   import { Provider } from 'react-redux';
   import { store } from './store/store';

   root.render(
     <Provider store={store}>
       <App />
     </Provider>
   );
   ```

5. Run the application:
   ```bash
   npm start
   ```

## Alternative: Use Your Own Setup

You can also:
- Use Spring Initializr (https://start.spring.io) for backend
- Use create-react-app directly
- Choose a different state management library (Zustand, Jotai, etc.)
- Organize your code differently

These templates are just to save you setup time!

## Next Steps

After setting up:
1. Review the database schema in `../DATABASE.md`
2. Create your entity models
3. Implement the required API endpoints
4. Build your frontend components
5. Connect everything together

Good luck! 🚀
