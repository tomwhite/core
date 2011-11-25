/*
 * @(#)IdentityHashtable.java	1.3 00/11/15
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans;

import java.util.*; 

/*
 * @version 1.3 11/15/00
 * @author Philip Milne
 */

class IdentityHashtable extends HashMap { 
    // private InstanceWrapper hashtableNull = new InstanceWrapper();
    
    private class InstanceWrapper { 
        public Object o; 
        
        public InstanceWrapper(Object o) { 
            this.o = o; 
        }

        public boolean equals(Object o2) { 
            return (o2.getClass() == InstanceWrapper.class) && (o == ((InstanceWrapper)o2).o); 
        }

        public int hashCode() { 
            return System.identityHashCode(o); 
        }
    }

    public Object put(Object key, Object value) { 
        return super.put(new InstanceWrapper(key), value); 
    }

    public Object get(Object key) { 
        return super.get(new InstanceWrapper(key)); 
    }

    public Object remove(Object key) { 
        return super.remove(new InstanceWrapper(key)); 
    } 
}
















































