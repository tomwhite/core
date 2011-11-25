/*
 * @(#)EventHandler.java	1.4 01/01/08
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans; 

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

import java.util.EventObject;

/**
 * The <code>EventHandler</code> class provides 
 * support for dynamically generating event listeners whose methods
 * execute a simple statement involving the incoming event object
 * and a target object. This is a specialization of what the <code>Proxy</code>
 * class does, in fact the <code>EventHandler</code> is 
 * implemented using the <code>Proxy</code> class.  
 * <p>
 * The <code>EventHandler</code> class is intended to be used by interactive tools, like
 * application builders, that allow developers to make <i>connections</i> between
 * beans. Typically connections are made from a user interface bean 
 * (the event source), to an application logic bean (the target). The most effective 
 * connections of this kind isolate the application logic from the user 
 * interface.  For example, the <code>EventHandler</code> for a 
 * connection from a <code>JCheckBox</code> to a method 
 * that accepts a boolean value can deal with extracting the state 
 * of the checkbox and passing it directly to the method so that 
 * the method is isolated from the user interface layer. 
 * <p>
 * Inner classes are a more general way to handle events from 
 * user interfaces, and the <code>EventHandler</code> class 
 * handles only a subset of what is possible using inner 
 * classes. One might nevertheless choose to use   
 * <code>EventHandler</code>s in a large application in 
 * which the same interface is implemented many times to 
 * reduce the disk and memory footprint of the application. 
 * <p>
 * The reason that <code>EventHandler</code>s have such a small 
 * footprint is that the <code>Proxy</code> class, on which 
 * the <code>EventHandler</code> is based, shares implementations 
 * of similar interfaces. It is, therefore, possible to implement,  
 * for example, all <code>ActionListener</code>s in an application  
 * with a single class. In general, listeners based on 
 * the <code>Proxy</code> class, require one listener class 
 * to be created per <em>listener type</em>, where the inner class 
 * approach requires one class to be created per <em>listener</em>. 
 * <p>
 * Additionally, both the <code>EventHandler</code> class and 
 * the <code>Proxy</code> class on which it depends have 
 * public factory methods to create instances of them and 
 * have no "hidden state". <code>EventHandler</code>s may 
 * therefore archived like other <i>JavaBeans</i> and appear 
 * in a textual form in the output of the new output 
 * streams.  
 * <p>
 * The simplest use of <code>EventHandler</code> is to install  
 * a listener that calls a method on the target object with no arguments.  
 * In the following example we create an <code>ActionListener</code> 
 * that calls the method named "toFront" on an instance 
 * of <code>javax.swing.JFrame</code>.
 * <pre>
 * <code>
 * myButton.addActionListener(
 *     (ActionListener)EventHandler.create(ActionListener.class, frame, "toFront"));
 * </code>
 * </pre>
 * When <code>myButton</code> is pressed, the statement 
 * <code>frame.toFront()</code> will be executed.  One could get 
 * the same effect, with some additional compile-time type safety, 
 * by defining a new implementation of the <code>ActionListener</code> 
 * interface and adding an instance of it to the button: 
 * <pre>
 * <code>
 * myButton.addActionListener(new ActionListener {
 *     public void actionPerformed(ActionEvent e) {
 *         frame.toFront();
 *     }
 * });
 * </code>
 * </pre> 
 * The next simplest use of <code>EventHandler</code> is 
 * to extract a property value from the first argument  
 * of the method in the listener interface (typically an event object) 
 * and use it to set the value of a property in the target object.  
 * In the following example we create an <code>ActionListener</code> that
 * sets the <code>nextFocusableComponent</code> property of the target 
 * object to the value of the "source" property of the event.
 * <pre>
 * <code>
 * EventHandler.create(ActionListener.class, target, "nextFocusableComponent", "source")
 * </code>
 * </pre>
 * This would correspond to the following inner class implementation: 
 * <pre>
 * <code>
 * new ActionListener {
 *     public void actionPerformed(ActionEvent e) {
 *         button.setNextFocusableComponent((Component)e.getSource()); 
 *     }
 * }
 * </code>
 * </pre>
 * Probably the most common use of <code>EventHandler</code> 
 * in the handling of the listener interfaces used in AWT 
 * and Swing is to extract a property value from the 
 * <em>source</em> of the event object and set this value as 
 * the value of a property of the target object. 
 * In the following example we create an <code>ActionListener</code> that
 * sets the "label" property of the target 
 * object to the value of the "text" property of the 
 * source (the value of the "source" property) of the event.
 * <pre>
 * <code>
 * EventHandler.create(ActionListener.class, button, "label", "source.text")
 * </code>
 * </pre>
 * This would correspond to the following inner class implementation: 
 * <pre>
 * <code>
 * new ActionListener {
 *     public void actionPerformed(ActionEvent e) {
 *         button.setLabel(((JTextField)e.getSource()).getText()); 
 *     }
 * }
 * </code>
 * </pre>
 * The event property may be be "qualified" with an arbitrary number 
 * of property prefixes delimited with the "." character. The "qualifying" 
 * names that appear before the "." characters are taken as the names of 
 * properties that should be applied, left-most first, to 
 * the event object.  
 * <p>  
 * For example, the following <code>EventHandler</code>: 
 * <pre>
 * <code>
 * EventHandler.create(ActionListener.class, target, "a", "b.c.d")
 * </code>
 * </pre>
 * could be written as the following inner class  
 * (assuming all the properties had canonical "getter" methods and 
 * returned the appropriate types): 
 * <pre>
 * <code>
 * new ActionListener {
 *     public void actionPerformed(ActionEvent e) {
 *         target.setA(e.getB().getC().getD()); 
 *     }
 * }
 * </code>
 * </pre>
 * 
 * @see java.lang.reflect.Proxy
 * @see java.util.EventObject
 * 
 * @since 1.4 
 * 
 * @author Mark Davidson
 * @author Philip Milne
 * @author Hans Muller
 *
 * @version 1.2 10/24/00
 */
public class EventHandler implements InvocationHandler {
    private static Object[] empty = new Object[]{};
    
    private Object target;
    private Method targetMethod;
    private String action;
    private String eventPropertyName;
    private String listenerMethodName; 
    
    /**
     * @param target the object that will perform the action.
     * @param action the (possibly qualified) name of a writable property or method on the target. 
     * @param eventPropertyName the (possibly qualified) name of a readable property of the incoming event. 
     * @param listenerMethodName the name of the method in the listener interface that should trigger the action.
     * 
     * @see EventHandler
     */
    public EventHandler(Object target, String action, String eventPropertyName, String listenerMethodName) {
        this.target = target;
        this.action = action;
        this.eventPropertyName = eventPropertyName;
        this.listenerMethodName = listenerMethodName;
    } 
    
    /**
     * Return the target of this handler. 
     * 
     * @return The target of this handler.  
     */
    public Object getTarget()  {
        return target;
    }
    
    /**
     * Return the action of this handler. 
     * 
     * @return The action of this handler.  
     * 
     * @see EventHandler
     */
    public String getAction()  {
        return action;
    }
    
    /**
     * Return the property of the event which should be 
     * used in the action applied to the target.  
     * 
     * @return Return the property of the event.  
     * 
     * @see EventHandler
     */
    public String getEventPropertyName()  {
        return eventPropertyName;
    }
    
    /**
     * Return the name of the method which will trigger the action.  
     * A return value of <code>null</code> signifies that all methods in this 
     * interface trigger the action.  
     * 
     * @return The name of the method which will trigger the action.  
     */
    public String getListenerMethodName()  {
        return listenerMethodName;
    } 
    
    private String capitalize(String propertyName) { 
        if (propertyName.length() == 0) { 
            return propertyName; 
        }
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    } 
    
    private Method getMethod(Class targetClass, String methodName, Class[] argClasses) { 
        return Statement.getMethod(targetClass, methodName, argClasses); 
    }
    
    private Object applyGetters(Object target, String getters) { 
        if (getters == null || getters.equals("")) { 
            return target; 
        }
        int firstDot = getters.indexOf('.'); 
        if (firstDot == -1) { 
            firstDot = getters.length(); 
        }
        String first = getters.substring(0, firstDot); 
        String rest = getters.substring(Math.min(firstDot + 1, getters.length())); 
        
        try { 
            Method getter = getMethod(target.getClass(), "get" + capitalize(first), new Class[]{});
            if (getter == null) { 
                getter = getMethod(target.getClass(), "is" + capitalize(first), new Class[]{});
            } 
            if (getter == null) { 
                getter = getMethod(target.getClass(), first, new Class[]{});
            } 
            if (getter == null) { 
                System.err.println("No method called: " + first + " defined on " + target); 
                return null; 
            } 
            Object newTarget = getter.invoke(target, new Object[]{}); 
            return applyGetters(newTarget, rest); 
        } 
        catch (Throwable e) { 
            System.out.println(e); 
            System.err.println("Failed to call method: " + first + " on " + target); 
        }
        return null; 
    }
    
    /**
     * Extract the appropriate property value from the event and 
     * pass it to the action associated with 
     * this <code>EventHandler</code>. 
     *
     * @param proxy the proxy object.
     * @param method the method in the listener interface. 
     * @return the result of applying the action to the target.  
     * 
     * @see EventHandler
     */
    public Object invoke(Object proxy, Method method, Object[] arguments) {

        String methodName = method.getName();
        if (method.getDeclaringClass() == Object.class)  {
            // Handle the Object public methods.
            if (methodName.equals("hashCode"))  {
                return new Integer(System.identityHashCode(proxy));   
            } else if (methodName.equals("equals")) {
                return (proxy == arguments[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
            }
        }

        if (listenerMethodName == null || listenerMethodName.equals(methodName)) {
                    
            Object source = ((EventObject)arguments[0]).getSource(); 
            
            Class[] argTypes = null; 
            Object[] newArgs = null; 

            if (eventPropertyName == null) {     // Nullary method. 
                newArgs = new Object[]{}; 
                argTypes = new Class[]{}; 
            }
            else { 
                Object input = applyGetters(arguments[0], getEventPropertyName()); 
                newArgs = new Object[]{input}; 
                argTypes = new Class[]{input.getClass()}; 
            }
            try {
                if (targetMethod == null) { 
                    targetMethod = getMethod(target.getClass(), action, argTypes);
                }
                if (targetMethod == null) { 
                    targetMethod = getMethod(target.getClass(), "set" + capitalize(action), argTypes);
                }
                if (targetMethod == null) { 
                    System.err.println("No target method called: " + action + " defined on class " + target.getClass() + " with argument type " + argTypes[0]); 
                }
                return targetMethod.invoke(target, newArgs);
            } 
            catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            catch (InvocationTargetException ex) {
                ex.getTargetException().printStackTrace();
            }
        }
        return null;
    }

    /**
     * Create an implementation of <i>listenerInterface</i> in which
     * <em>all</em> of the methods in the listener interface apply 
     * the handler's <i>action</i> to the <i>target</i>. This 
     * method is implemented by calling the, more general, 
     * implementation of the <code>create</code> method with both 
     * the <code>eventPropertyName</code> and the <code>listenerMethodName</code> 
     * taking the value <code>null</code>. 
     * <p>
     * To create an <code>ActionListener</code> that showed a 
     * <code>JDialog</code> with <code>dialog.show()</code>, 
     * one could write:
     * <pre>
     * <code>
     * EventHandler.create(ActionListener.class, dialog, "show");
     * </code>
     * </pre>
     * <p>
     * @param listenerInterface The listener interface to create a proxy for.
     * @param target the object that will perform the action.
     * @param action the name of a writable property or method on the target. 
     * 
     * @return an object that implements <code>listenerInterface</code>. 
     * 
     * @see #create(Class, Object, String, String)
     */
    public static Object create(Class listenerInterface, Object target, String action) {
        return create(listenerInterface, target, action, null, null); 
    }

    /**
     * Create an implementation of <i>listenerInterface</i> in which
     * <em>all</em> of the methods pass the value of the event 
     * expression, <i>eventPropertyName</i>, to the final method in the
     * statement, <i>action</i>, which is applied to the <i>target</i>.
     * This method is implemented by calling the, 
     * more general, implementation of the <code>create</code> method with 
     * the <code>listenerMethodName<code> taking the value <code>null</code>. 
     * <p>
     * To create an <code>ActionListener</code> that sets the
     * the text of a <code>JLabel</code> to the text value of 
     * the <code>JTextField</code> source of the incoming event as in: 
     * <pre>
     * <code>
     * label.setText((JTextField(event.getSource())).getText()) 
     * </code>
     * </pre>
     * one could write:
     * <pre>
     * <code>
     * EventHandler.create(ActionListener.class, label, "text", "source.text");
     * </code>
     * </pre>
     * 
     * @param listenerInterface the listener interface to create a proxy for.
     * @param target the object that will perform the action.
     * @param action the name of a writable property or method on the target. 
     * @param eventPropertyName the (possibly qualified) name of a readable property of the incoming event. 
     * 
     * @return an object that implements <code>listenerInterface</code>
     * 
     * @see #create(Class, Object, String, String, String)
     */
    public static Object create(Class listenerInterface, Object target, String action, String eventPropertyName) {
        return create(listenerInterface, target, action, eventPropertyName, null); 
    }

    /**
     * Create an implementation of <i>listenerInterface</i> in which
     * the listener method named <i>listenerMethodName</i> 
     * passes the value of the event expression, <i>eventPropertyName</i>, 
     * to the final method in the statement, <i>action</i>, which 
     * is applied to the <i>target</i>. All of the other listener 
     * methods do nothing. 
     * <p>
     * If the <i>eventPropertyName</i> is <code>null</code> the 
     * implementation will call a method with the name specified 
     * in <i>action</i> which takes an <code>EventObject</code> 
     * or a nullary method with the same name if a method 
     * accepting an <code>EventObject</code> is not defined. 
     * <p>
     * If the <i>listenerMethodName</i> is <code>null</code> 
     * <em>all</em> methods in the interface trigger the <i>action</i> to be 
     * executed on the <i>target</i>. 
     * <p>
     * For example, to create a <code>MouseListener</code> that sets the target
     * object's <code>origin</code> property to the incoming <code>MouseEvent</code>'s
     * location (that's the value of <code>mouseEvent.getPoint()</code>) each 
     * time a mouse button is pressed, one would write:
     * <pre>
     * <code>
     * EventHandler.create(MouseListener.class, "mousePressed", target, "origin", "point");
     * </code>
     * </pre>
     * This is comparable to writing a <code>MouseListener</code> in which all
     * of the methods except <code>mousePressed</code> are no-ops:
     * <pre>
     * <code>
     * new MouseAdapter() {
     *     public void mousePressed(MouseEvent e) {
     *         target.setOrigin(e.getPoint());
     *     }
     * }
     * </code>
     * </pre>
     * 
     * @param listenerInterface the listener interface to create a proxy for.
     * @param target the object that will perform the action.
     * @param action the name of a writable property or method on the target. 
     * @param eventPropertyName the (possibly qualified) name of a readable property of the incoming event. 
     * @param listenerMethodName the name of the method in the listener interface that should trigger the action.
     * 
     * @return an object that implements <code>listenerInterface</code>. 
     * 
     * @see EventHandler
     */
    public static Object create(Class listenerInterface, Object target, String action, String eventPropertyName, String listenerMethodName) {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), 
                                      new Class[] {listenerInterface}, 
                                      new EventHandler(target, action, 
                                                          eventPropertyName, listenerMethodName));
    }
}





