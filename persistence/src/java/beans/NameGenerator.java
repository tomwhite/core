/*
 * @(#)NameGenerator.java	1.3 00/11/15
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

class NameGenerator { 

    private static HashMap valueToName; 
    private static HashMap instanceCountsByClass; 
    
    static { 
        init(); 
    }
    
    private static void init() { 
        valueToName = new IdentityHashtable();
        instanceCountsByClass = new IdentityHashtable();
    }
    
    static void clear() { 
        init(); 
    }
    
    private static String unqualifiedClassName(Class type) { 
        if (type.isArray()) {
            return unqualifiedClassName(type.getComponentType())+"Array"; 
        }
        String name = type.getName(); 
        return name.substring(name.lastIndexOf('.')+1); 
    }
    
    static String replace(String s, char out, String in) { 
        StringBuffer result = new StringBuffer(); 
        for(int i = 0; i < s.length(); i++) { 
            if (s.charAt(i) != out) { 
                result.append(s.charAt(i)); 
            }
            else { 
                result.append(in); 
            }
        } 
        return result.toString(); 
    }

    static String instanceName(Object instance) { 
        
        if (instance == null) {
            return "null"; 
        }
        if (instance.getClass() == String.class) { 
            return "\""+(String)instance + "\""; 
        }
        if (instance instanceof Class) {
            return unqualifiedClassName((Class)instance); 
        }
        else { 
            String result = (String)valueToName.get(instance); 
            if (result != null) { 
                return result; 
            }
            Class type = instance.getClass(); 
            Object size = instanceCountsByClass.get(type); 
            int instanceNumber = (size == null) ? 0 : ((Integer)size).intValue() + 1; 
            instanceCountsByClass.put(type, new Integer(instanceNumber)); 
            result = unqualifiedClassName(type) + instanceNumber; 
            valueToName.put(instance, result); 
            return result; 
        }
    }
}
















































