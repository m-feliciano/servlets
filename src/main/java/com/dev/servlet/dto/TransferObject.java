package com.dev.servlet.dto;

import com.dev.servlet.model.Identifier;

import java.io.Serializable;

/**
 * This class is a base class for all transfer objects (DTOs).
 */
public abstract class TransferObject<U extends Serializable> implements Identifier<U>, Serializable {
}
