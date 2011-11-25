/*
 * @(#)DefaultPersistenceDelegate.java	1.6 00/11/28
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans;

import java.util.*;
import java.lang.reflect.*;
import java.beans.*;
import java.io.*;


/**
 * The <code>DefaultPersistenceDelegate</code> is a concrete implementation of 
 * the abstract <code>PersistenceDelegate</code> class and 
 * is the delegate used by default for classes about 
 * which no information is available. The <code>DefaultPersistenceDelegate</code> 
 * provides, version resilient, public API-based persistence for 
 * classes that follow the JavaBeans conventions without any class specific 
 * configuration. 
 * <p>
 * The key assumptions are that the class has a nullary constructor 
 * and that its state is accurately represented by matching pairs 
 * of "setter" and "getter" methods in the order they are returned 
 * by the Introspector. 
 * In addition to providing code-free persistence for JavaBeans, 
 * the <code>DefaultPersistenceDelegate</code> provides a convenient means 
 * to effect persistent storage for classes that have a constructor 
 * that, while not nullary, simply requires some property values 
 * as arguments. 
 *
 * @see #DefaultPersistenceDelegate(String[])
 * @see java.beans.Introspector
 *
 * @since 1.4
 * 
 * @version 1.6 11/28/00
 * @author Philip Milne
 */
    
public class DefaultPersistenceDelegate extends PersistenceDelegate { 
    private String[] constructor; 

    /**
     * Creates a persistence delegate for a class with a nullary constructor. 
     *
     * @see #DefaultPersistenceDelegate(java.lang.String[])
     */
    public DefaultPersistenceDelegate() { 
        this(new String[0]); 
    } 
    
    /**
     * Creates a default persistence delegate for a class with a 
     * constructor whose arguments are the values of the property 
     * names as specified by <code>constructorPropertyNames</code>. 
     * The constructor arguments are created by 
     * evaluating the property names in the order they are supplied. 
     * To use this class to specify a single preferred constructor for use 
     * in the serialization of a particular type, we state the 
     * names of the properties that make up the constructor's 
     * arguments. For example, the <code>Font</code> class which 
     * does not define a nullary constructor can be handled 
     * with the following persistence delegate: 
     *
     * <pre>
     *     new DefaultPersistenceDelegate(new String[]{"name", "style", "size"});
     * </pre> 
     *
     * @param  constructorPropertyNames The property names for the arguments of this constructor. 
     *
     * @see #instantiate
     */
    public DefaultPersistenceDelegate(String[] constructorPropertyNames) {
        this.constructor = constructorPropertyNames; 
    }
    
    private String capitalize(String propertyName) {
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    } 
           
    /**
     * If the number of arguments in the specified constructor is non-zero 
     * this method returns true if, and only if, 
     * <code>oldInstance.equals(newInstance)</code>. Otherwise, this method  
     * uses the superclass's definition which returns true if the 
     * classes of the two instances are equal. 
     *
     * @param oldInstance The instance to be copied. 
     * @param newInstance The instance that is to be modified. 
     * @return True if an equivalent copy of <code>newInstance</code> may be 
     *         created by applying a series of mutations to <code>oldInstance</code>. 
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected boolean mutatesTo(Object oldInstance, Object newInstance) { 
        // Assume the instance is either mutable or a singleton 
        // if it has a nullary constructor. 
        return (constructor.length == 0) ? 
            super.mutatesTo(oldInstance, newInstance) :  
            oldInstance.equals(newInstance);  
    } 
    
    /**
     * This default implementation of the <code>instantiate</code> method returns  
     * an expression containing the predefined method name "new" which denotes a 
     * call to a constructor with the arguments as specified in  
     * the <code>DefaultPersistenceDelegate</code>'s constructor. 
     *
     * @param  oldInstance The instance to be instantiated. 
     * @param  out The code output stream. 
     * @return An expression whose value is <code>oldInstance</code>. 
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected Expression instantiate(Object oldInstance, Encoder out) { 
        int nArgs = constructor.length; 
        Class type = oldInstance.getClass(); 
        // System.out.println("writeObject: " + oldInstance); 
        Object[] constructorArgs = new Object[nArgs]; 
        for(int i = 0; i < nArgs; i++) { 
            /*
            1.2 introduces "public double getX()" et al. which return values 
            which cannot be used in the constructors (they are the wrong type). 
            In constructors, use public fields in preference to getters 
            when they are defined. 
            */
            String name = constructor[i];
        
            Field f = null; 
            try { 
                // System.out.println("Trying field " + name + " in " + type); 
                f = type.getDeclaredField(name); 
                f.setAccessible(true);    
            }
            catch (NoSuchFieldException e) {} 
            try { 
                constructorArgs[i] = (f != null && !Modifier.isStatic(f.getModifiers())) ? 
                    f.get(oldInstance) :   
                    type.getMethod("get"+capitalize(name), new Class[0]).invoke(oldInstance, new Object[0]);
            }
            catch (Exception e) { 
                // handleError(e, "Warning: Failed to get " + name + " property for " + oldInstance.getClass() + " constructor"); 
                out.getExceptionListener().exceptionThrown(e); 
            }
        } 
        return new Expression(oldInstance, oldInstance.getClass(), "new", constructorArgs); 
    } 
    
    // This is a workaround for a bug in the introspector.  
    // PropertyDescriptors are not shared amongst subclasses. 
    private boolean isTransient(Class type, PropertyDescriptor pd) { 
        if (type == null) { 
            return false; 
        }
        // This code was mistakenly deleted - it may be fine and 
        // is more efficient than the code below. This should 
        // all disappear anyway when property descriptors are shared 
        // by the introspector. 
        /*
        Method getter = pd.getReadMethod();
        Class declaringClass = getter.getDeclaringClass(); 
        if (declaringClass == type) { 
            return Boolean.TRUE.equals(pd.getValue("transient"));    
        } 
        */
        String pName = pd.getName(); 
        BeanInfo info = MetaData.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; ++i ) { 
            PropertyDescriptor pd2 = propertyDescriptors[i]; 
            if (pName.equals(pd2.getName())) { 
                Object value = pd2.getValue("transient"); 
                if (value != null) { 
                    return Boolean.TRUE.equals(value); 
                }
            }
        } 
        return isTransient(type.getSuperclass(), pd); 
    }
    
    private static boolean equals(Object o1, Object o2) { 
        return (o1 == null) ? (o2 == null) : o1.equals(o2);  
    }
    
    private void doProperty(Class type, PropertyDescriptor pd, Object oldInstance, Object newInstance, Encoder out) throws Exception { 
        Method getter = pd.getReadMethod();
        Method setter = pd.getWriteMethod();
        
        if (getter != null && setter != null && !isTransient(type, pd)) { 
            Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{}); 
            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{}); 
            Object oldValue = oldGetExp.getValue();  
            Object newValue = newGetExp.getValue();  
            out.writeExpression(oldGetExp); 
            if (!equals(newValue, out.get(oldValue))) { 
                // System.out.println("Not equal: " + actualGetExp + " != " + newGetExp); 
                invokeStatement(oldInstance, setter.getName(), new Object[]{oldValue}, out);
            }
        }
    }
    
    static void invokeStatement(Object instance, String methodName, Object[] args, Encoder out) { 
        out.writeStatement(new Statement(instance, methodName, args)); 
    }
        
    // Write out the properties of this instance.                 
    private void initBean(Class type, Object oldInstance, Object newInstance, Encoder out) { 
        // System.out.println("initBean: " + oldInstance); 
        BeanInfo info = MetaData.getBeanInfo(type); 
        
        // Properties
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; ++i ) { 
            try { 
                doProperty(type, propertyDescriptors[i], oldInstance, newInstance, out);
            }
            catch (Exception e) { 
                out.getExceptionListener().exceptionThrown(e);  
            }
        }
        
        // Listeners
        if (!java.awt.Component.class.isAssignableFrom(type)) { 
            return; // Just handle the listeners of Components for now. 
        } 
        EventSetDescriptor[] eventSetDescriptors = info.getEventSetDescriptors(); 
        for (int e = 0; e < eventSetDescriptors.length; e++) { 
            EventSetDescriptor d = eventSetDescriptors[e]; 
            Class listenerType = d.getListenerType(); 
            
            if (listenerType == java.awt.event.ComponentListener.class) { 
                continue; 
            }
            
            EventListener[] oldL = new EventListener[0]; 
            EventListener[] newL = new EventListener[0]; 
            try { 
                Method m = type.getMethod("getListeners", new Class[]{Class.class}); 
                oldL = (EventListener[])m.invoke(oldInstance, new Object[]{listenerType}); 
                newL = (EventListener[])m.invoke(newInstance, new Object[]{listenerType}); 
            } 
            catch (Exception e2) { 
                return; 
            }
            
            // Asssume the listeners are in the same order and that there are no gaps. 
            // Eventually, this may need to do true differencing. 
            String addListenerMethodName = d.getAddListenerMethod().getName(); 
            for (int i = newL.length; i < oldL.length; i++) { 
                // System.out.println("Adding listener: " + addListenerMethodName + oldL[i]); 
                invokeStatement(oldInstance, addListenerMethodName, new Object[]{oldL[i]}, out);
            }
            
            String removeListenerMethodName = d.getRemoveListenerMethod().getName(); 
            for (int i = oldL.length; i < newL.length; i++) {
                invokeStatement(oldInstance, removeListenerMethodName, new Object[]{oldL[i]}, out);
            }
        }
    }
    
    /**
     * This default implementation of the <code>initialize</code> method assumes 
     * all state held in objects of this type is exposed via the 
     * matching pairs of "setter" and "getter" methods in the order 
     * they are returned by the Introspector. If a property descriptor 
     * defines a "transient" attribute with a value equal to 
     * <code>Boolean.TRUE</code> the property is ignored by this 
     * default implementation. Note that this use of the word 
     * "transient" is quite independent of the field modifier 
     * that is used by the <code>ObjectOutputStream</code>.
     * <p>
     * For each non-transient property, an expression is created 
     * in which the nullary "getter" method is applied 
     * to the <code>oldInstance</code>. The value of this 
     * expression is the value of the property in the instance that is 
     * being serialized. If the value of this expression 
     * in the cloned environment <code>mutatesTo</code> the 
     * target value, the new value is initialized to make it 
     * equivalent to the old value. In this case, because 
     * the property value has not changed there is no need to 
     * call the corresponding "setter" method and no statement 
     * is emitted. If not however, the expression for this value 
     * is replaced with another expression (normally a constructor) 
     * and the corresponding "setter" method is called to install 
     * the new property value in the object. This scheme removes 
     * default information from the output produced by streams 
     * using this delegate. 
     * <p>
     * In passing these statements to the output stream, where they 
     * will be executed, side effects are made to the <code>newInstance</code>. 
     * In most cases this allows the problem of properties 
     * whose values depend on each other to actually help the 
     * serialization process by making the number of statements  
     * that need to be written to the output smaller. In general, 
     * the problem of handling interdependent properties is reduced to 
     * that of finding an order for the properties in 
     * a class such that no property value depends on the value of 
     * a subsequent property. 
     * 
     * @param oldInstance The instance to be copied. 
     * @param newInstance The instance that is to be modified. 
     * @param out The stream to which any initialization statements should be written. 
     * 
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.PropertyDescriptor
     */ 
    protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
        // System.out.println("DefulatPD:initialize" + type); 
        super.initialize(type, oldInstance, newInstance, out);
        if (oldInstance.getClass() == type) { // !type.isInterface()) { 
            initBean(type, oldInstance, newInstance, out); 
        }
    } 
}
