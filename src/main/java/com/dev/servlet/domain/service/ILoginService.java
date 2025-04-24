package com.dev.servlet.domain.service;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;

/**
 * Service interface for managing user authentication and session operations.
 * 
 * <p>This interface defines the contract for login and logout operations,
 * handling user authentication, session management, and security validations.
 * It serves as the main entry point for user authentication processes in
 * the application, coordinating with user services for credential validation.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface ILoginService {
    
    /**
     * Authenticates a user and establishes a session based on the provided credentials.
     * This method coordinates with the user service to validate credentials and
     * create appropriate session data.
     *
     * @param request the request containing login credentials
     * @param userService the user service for credential validation
     * @return UserDTO representing the authenticated user with session information
     * @throws ServiceException if authentication fails due to invalid credentials or system errors
     */
    UserDTO login(Request request, IUserService userService) throws ServiceException;
    
    /**
     * Terminates the user session and performs cleanup operations.
     * This method handles session invalidation and any necessary logout procedures.
     *
     * @param request the request containing session information to be terminated
     */
    void logout(Request request);
}
