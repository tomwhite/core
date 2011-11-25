/*
 * @(#)ExceptionListener.java	1.3 00/11/15
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans;

/**
 * An ExceptionListener is notified of internal exceptions. 
 * 
 * @since 1.4
 *
 * @version 1.3 11/15/00
 * @author Philip Milne
 */        
public interface ExceptionListener { 
    /**
     * This method is called when a recoverable exception has 
     * been caught. 
     *
     * @param e The exception that was caught. 
     * 
     */
    public void exceptionThrown(Exception e); 
}















































