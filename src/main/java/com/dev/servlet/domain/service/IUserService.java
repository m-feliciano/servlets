package com.dev.servlet.domain.service;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.User;
import java.util.Optional;

/**
 * Service interface for managing user operations in the servlet application.
 * 
 * <p>This interface defines the contract for all user-related business operations,
 * including user registration, authentication, profile management, and validation.
 * It handles user lifecycle management and provides essential user services
 * for the application's security and user management features.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IUserService {
    
    /**
     * Checks if an email address is available for registration by comparing it
     * against existing users, excluding the candidate user if provided.
     *
     * @param email the email address to check for availability
     * @param candidate the user to exclude from the check (for updates), can be null
     * @return true if email is available, false if already taken
     */
    boolean isEmailAvailable(String email, User candidate);
    
    /**
     * Registers a new user in the system based on the provided request data.
     *
     * @param request the request containing user registration information
     * @return UserDTO representing the newly registered user
     * @throws ServiceException if registration fails due to validation or business rule violations
     */
    UserDTO register(Request request) throws ServiceException;
    
    /**
     * Updates an existing user's information with new data from the request.
     *
     * @param request the request containing updated user data
     * @return UserDTO representing the updated user
     * @throws ServiceException if update fails due to validation or business rule violations
     */
    UserDTO update(Request request) throws ServiceException;
    
    /**
     * Retrieves a user by their identifier from the request.
     *
     * @param request the request containing the user ID
     * @return UserDTO representing the found user
     * @throws ServiceException if user is not found or request is invalid
     */
    UserDTO getById(Request request) throws ServiceException;
    
    /**
     * Deletes a user identified by the request data.
     *
     * @param request the request containing user deletion criteria
     * @return true if deletion was successful, false otherwise
     * @throws ServiceException if deletion fails due to business constraints or dependencies
     */
    boolean delete(Request request) throws ServiceException;
    
    /**
     * Finds a user by their login credentials (username/email and password).
     * Used primarily for authentication purposes.
     *
     * @param login the user's login identifier (username or email)
     * @param password the user's password
     * @return Optional containing the User if found and credentials match, empty otherwise
     */
    Optional<User> findByLoginAndPassword(String login, String password);
}
