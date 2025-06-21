package com.dev.servlet.application.transfer.dto;


import com.dev.servlet.domain.model.Entity;

import java.io.Serializable;

/**
 * This class is a base class for all transfer objects (DTOs).
 */
public abstract class DataTransferObject<ID> implements Entity<ID>, Serializable {
}