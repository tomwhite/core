/*
 * @(#)MetaData.java	1.3 00/11/15
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.beans;

import java.io.*;

import java.util.*;
import java.lang.reflect.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.plaf.*;

/*
 * Like the <code>Intropector</code>, the <code>MetaData</code> class 
 * contains <em>meta</em> objects that describe the way 
 * classes should express their state in terms of their 
 * own public APIs.   
 *
 * @see java.beans.Intropector
 *
 * @version 1.3 11/15/00
 * @author Philip Milne
 * @author Steve Langley
 */
    
class MetaData { 
    // Make it clear this instances of class should not be created. 
    private MetaData() {}

    private static Hashtable classToBeanInfo = new Hashtable(); 
    
    private static boolean equals(Object o1, Object o2) { 
        return (o1 == null) ? (o2 == null) : o1.equals(o2);  
    }
    
    private static PersistenceDelegate nullPersistenceDelegate = new PersistenceDelegate() {
        // Note this will be called by all classes when they reach the 
        // top of their superclass chain. 
        protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
        }
        protected Expression instantiate(Object oldInstance, Encoder out) { return null; } 

        public void writeObject(Object oldInstance, Encoder out) { 
        // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance); 
        }
    };
    
    private static PersistenceDelegate primitivePersistenceDelegate = new PersistenceDelegate() {
        protected boolean mutatesTo(Object oldInstance, Object newInstance) { 
            return oldInstance.equals(newInstance); 
        }

        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, oldInstance.getClass(), 
                      "new", new Object[]{oldInstance.toString()});
        }
    };
        
    private static PersistenceDelegate arrayPersistenceDelegate = new PersistenceDelegate() {
        protected boolean mutatesTo(Object oldInstance, Object newInstance) { 
            return (newInstance != null && 
                    oldInstance.getClass() == newInstance.getClass() && // Also ensures the subtype is correct. 
                    Array.getLength(oldInstance) == Array.getLength(newInstance)); 
            }

        protected Expression instantiate(Object oldInstance, Encoder out) {
            // System.out.println("instantiate: " + type + " " + oldInstance);
            Class oldClass = oldInstance.getClass(); 
            return new Expression(oldInstance, Array.class, "newInstance", 
                       new Object[]{oldClass.getComponentType(), 
                                    new Integer(Array.getLength(oldInstance))}); 
            }

        protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
            int n = Array.getLength(oldInstance);
            for (int i = 0; i < n; i++) { 
                Object index = new Integer(i); 
                // Expression oldGetExp = new Expression(Array.class, "get", new Object[]{oldInstance, index}); 
                // Expression newGetExp = new Expression(Array.class, "get", new Object[]{newInstance, index}); 
                Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index}); 
                Expression newGetExp = new Expression(newInstance, "get", new Object[]{index}); 
                try { 
                    Object oldValue = oldGetExp.getValue();  
                    Object newValue = newGetExp.getValue();  
                    out.writeExpression(oldGetExp); 
                    if (!MetaData.equals(newValue, out.get(oldValue))) {
                        // System.out.println("Not equal: " + newGetExp + " != " + actualGetExp); 
                        // invokeStatement(Array.class, "set", new Object[]{oldInstance, index, oldValue}, out);
                        DefaultPersistenceDelegate.invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                    }
                }
                catch (Exception e) { 
                    // System.err.println("Warning:: failed to write: " + oldGetExp); 
                    out.getExceptionListener().exceptionThrown(e); 
                }
            }
        }
    };

    private static PersistenceDelegate proxyPersistenceDelegate = new PersistenceDelegate() {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Class type = oldInstance.getClass(); 
            Proxy p = (Proxy)oldInstance; 
            // This unappealing hack is not required but makes the 
            // representation of EventHandlers much more concise. 
            InvocationHandler ih = Proxy.getInvocationHandler(p); 
            if (ih instanceof EventHandler) { 
                EventHandler eh = (EventHandler)ih; 
                Vector args = new Vector(); 
                args.add(type.getInterfaces()[0]); 
                args.add(eh.getTarget()); 
                args.add(eh.getAction()); 
                if (eh.getEventPropertyName() != null) { 
                    args.add(eh.getEventPropertyName()); 
                } 
                if (eh.getListenerMethodName() != null) { 
                    args.setSize(4); 
                    args.add(eh.getListenerMethodName()); 
                } 
                return new Expression(oldInstance, 
                                      EventHandler.class,
                                      "create", 
                                      args.toArray());
                }
            return new Expression(oldInstance, 
                                  Proxy.class,
                                  "newProxyInstance", 
                                  new Object[]{type.getClassLoader(),
                                               type.getInterfaces(),
                                               ih});
        }
    }; 
        
    private static PersistenceDelegate defaultPersistenceDelegate = new DefaultPersistenceDelegate(); 
    
    // Duplicated in evaluator. 
    private static Class typeToClass(Class type) {
        if (!type.isPrimitive()) return type;
        if (type == Boolean.TYPE) return Boolean.class;
        if (type == Byte.TYPE) return Byte.class;
        if (type == Character.TYPE) return Character.class;
        if (type == Short.TYPE) return Short.class;
        if (type == Integer.TYPE) return Integer.class;
        if (type == Long.TYPE) return Long.class;
        if (type == Float.TYPE) return Float.class;
        if (type == Double.TYPE) return Double.class;
        if (type == Void.TYPE) return Void.class;
        return null;
    }

    private static Field typeToField(Class type) {
        try { 
            return typeToClass(type).getDeclaredField("TYPE"); 
        }
        catch (NoSuchFieldException e) { 
            return null; 
        }
    }

    private static Object getPrivateField(Object instance, Class declaringClass, String name, ExceptionListener el) { 
        try {
            Field f = declaringClass.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(instance);
        }
        catch (Exception e) {
            el.exceptionThrown(e);
        }
        return null;
    }
           
    static {
        
        // Primitives, Arrays and Proxies are handled dynamically.
        
        // Strings
        setPersistenceDelegate(String.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) { return null; } 
            
            public void writeObject(Object oldInstance, Encoder out) { 
                // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance); 
            }
        });

        // Classes
        setPersistenceDelegate(Class.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                Class c = (Class)oldInstance; 
                // As of 1.3 it is not possible to call Class.forName("int"), 
                // so we have to generate different code for primitive types. 
                // This is needed for arrays whose subtype may be primitive. 
                if (c.isPrimitive()) { 
                    Field field = typeToField(c); 
                    if (field == null) { 
                        System.err.println("Unknown primitive type: " + c); 
                    }
                    return new Expression(oldInstance, field, "get", new Object[]{null}); 
                }
                else if (oldInstance == String.class) { 
                    return new Expression(oldInstance, "", "getClass", new Object[]{});
                }
                else if (oldInstance == Class.class) { 
                    return new Expression(oldInstance, String.class, "getClass", new Object[]{});
                }
                else { 
                    return new Expression(oldInstance, Class.class, "forName", new Object[]{c.getName()}); 
                }
            }
        });

        // Fields
        setPersistenceDelegate(Field.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                Field f = (Field)oldInstance;
                return new Expression(oldInstance, 
                        f.getDeclaringClass(), 
                        "getField", 
                        new Object[]{f.getName()});
            }
        });

        // Methods
        setPersistenceDelegate(Method.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                Method m = (Method)oldInstance; 
                return new Expression(oldInstance, 
                        m.getDeclaringClass(), 
                        "getMethod", 
                        new Object[]{m.getName(), m.getParameterTypes()});
            }
        });

// Util

        // Vectors, etc. 
        setPersistenceDelegate(java.util.List.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                java.util.List oldO = (java.util.List)oldInstance;
                java.util.List newO = (java.util.List)newInstance;
                int oldSize = oldO.size(); 
                int newSize = (newO == null) ? 0 : newO.size(); 
                if (oldSize < newSize) { 
                    invokeStatement(oldInstance, "setSize", new Object[]{new Integer(oldSize)}, out);
                    newSize = oldSize; 
                }
                for (int i = 0; i < newSize; i++) { 
                    Object index = new Integer(i); 
                    
                    Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index}); 
                    Expression newGetExp = new Expression(newInstance, "get", new Object[]{index}); 
                    try { 
                        Object oldValue = oldGetExp.getValue();  
                        Object newValue = newGetExp.getValue();  
                        out.writeExpression(oldGetExp); 
                        if (!MetaData.equals(newValue, out.get(oldValue))) {
                            invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                        }
                    } 
                    catch (Exception e) { 
                        out.getExceptionListener().exceptionThrown(e); 
                    }
                }
                for (int i = newSize; i < oldSize; i++) {
                    invokeStatement(oldInstance, "add", new Object[]{oldO.get(i)}, out);
                }
            }

        });


        // Hashtables, etc.
        setPersistenceDelegate(Map.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                // System.out.println("Initializing: " + newInstance); 
                Map oldMap = (Map)oldInstance;
                Map newMap = (Map)newInstance;
                // Remove the new elements. 
                // Do this first otherwise we undo the adding work. 
                if (newMap != null) {
                    Iterator newKeys = newMap.keySet().iterator();
                    while(newKeys.hasNext()) {
                        Object newKey = newKeys.next(); 
                       // PENDING: This "key" is not in the right environment. 
                        if (!oldMap.containsKey(newKey)) {  
                            invokeStatement(oldInstance, "remove", new Object[]{newKey}, out);
                        }
                    }
                }
                // Add the new elements. 
                Iterator oldKeys = oldMap.keySet().iterator();
                while(oldKeys.hasNext()) {
                    Object oldKey = oldKeys.next(); 
                                        
                    Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{oldKey}); 
                    // Pending: should use newKey. 
                    Expression newGetExp = new Expression(newInstance, "get", new Object[]{oldKey}); 
                    try { 
                        Object oldValue = oldGetExp.getValue();  
                        Object newValue = newGetExp.getValue();  
                        out.writeExpression(oldGetExp); 
                        if (!MetaData.equals(newValue, out.get(oldValue))) {
                            invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                        }
                    } 
                    catch (Exception e) { 
                        out.getExceptionListener().exceptionThrown(e); 
                    }
                }
            }
        }); 

// AWT

    // Geometry: Dimension, Rectangle and Point
                
        registerConstructor(Point.class, new String[]{"x", "y"});
        registerConstructor(Dimension.class, new String[]{"width", "height"});
        registerConstructor(Rectangle.class, new String[]{"x", "y", "width", "height"});

    // Miscellany 
       
        // SystemColors
        setPersistenceDelegate(SystemColor.class, new PersistenceDelegate() { 
            Hashtable valueToField; 
            
            public Hashtable getValueToField() { 
                if (valueToField == null) { 
                    valueToField = new Hashtable(); 
                    Field fields[] = SystemColor.class.getFields(); 
                    for(int i = 0; i < fields.length; i++) { 
                        Field f = fields[i];
                        try  { 
                            Object value = f.get(null); 
                            valueToField.put(value, f); 
                        }
                        catch (IllegalArgumentException e) {}
                        catch (IllegalAccessException e) {}
                    }
                }
                return valueToField;
            }
            
            protected Expression instantiate(Object oldInstance, Encoder out) {
                Field f = (Field)getValueToField().get(oldInstance); 
                if (f != null) { 
                    return new Expression(oldInstance, f, "get", new Object[]{null}); 
                }
                return null; 
            }
        });
        
        // Component 
        setPersistenceDelegate(Component.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                Component c = (Component)oldInstance; 
                Component c2 = (Component)newInstance; 
                // The "background", "foreground" and "font" properties. 
                // The foreground and font properties of Windows change from 
                // null to defined values after the Windows are made visible - 
                // special case them for now.  
                if (!(oldInstance instanceof Window)) { 
                    String[] fieldNames = new String[]{"background", "foreground", "font"}; 
                    for(int i = 0; i < fieldNames.length; i++) { 
                        String name = fieldNames[i]; 
                        Object oldValue = getPrivateField(oldInstance, Component.class, name, out.getExceptionListener()); 
                        Object newValue = (newInstance == null) ? null : getPrivateField(newInstance, Component.class, name, out.getExceptionListener()); 
                        if (oldValue != null && !oldValue.equals(newValue)) { 
                            invokeStatement(oldInstance, "set" + capitalize(name), new Object[]{oldValue}, out);
                        }
                    }
                }     
                
                // Bounds 
                Container p = c.getParent(); 
                if ((p == null || p.getLayout() == null) && 
                    (c2 == null || !c.getBounds().equals(c2.getBounds()))) { 
                    invokeStatement(oldInstance, "setBounds", new Object[]{c.getBounds()}, out);
                }
            }
        }); 
        
        // Container
        setPersistenceDelegate(Container.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                // Ignore the children of a JScrollPane. 
                // Pending(milne) find a better way to do this. 
                if (oldInstance instanceof JScrollPane) { 
                    return; 
                }
                Container oldC = (Container)oldInstance;
                Component[] oldChildren = oldC.getComponents();
                Container newC = (Container)newInstance;
                Component[] newChildren = (newC == null) ? new Component[0] : newC.getComponents(); 
                // Pending. Assume all the new children are unaltered.  
                for (int i = newChildren.length; i < oldChildren.length; i++) {
                    invokeStatement(oldInstance, "add", new Object[]{oldChildren[i]}, out);
                }
            }
        });

        // JFrame (If we do this for Window instead of JFrame, the setVisible call 
        // will be issued before we have added all the children to the JFrame and 
        // will appear blank). 
        setPersistenceDelegate(JFrame.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                Window oldC = (Window)oldInstance;
                Window newC = (Window)newInstance; 
                boolean oldV = oldC.isVisible(); 
                boolean newV = newC.isVisible();             
                if (newV != oldV) {
                    // false means: don't execute this statement at write time.
                    boolean executeStatements = out.executeStatements; 
                    out.executeStatements = false; 
                    invokeStatement(oldInstance, "setVisible", new Object[]{new Boolean(oldV)}, out);
                    out.executeStatements = executeStatements; 
                }
            }
        });

    // LayoutManagers
    
        // BorderLayout
        setPersistenceDelegate(BorderLayout.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                BorderLayout oldBL = (BorderLayout)oldInstance;
                BorderLayout newBL = (BorderLayout)newInstance;
                String[] locations = {"north", "south", "east", "west", "center"}; 
                String[] names = {BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST, BorderLayout.WEST, BorderLayout.CENTER}; 
                for (int i = 0; i < locations.length; i++) { 
                    Component oldC = (Component)getPrivateField(oldBL, BorderLayout.class, locations[i], out.getExceptionListener());
                    Component newC = (Component)getPrivateField(newBL, BorderLayout.class, locations[i], out.getExceptionListener());
                    // Pending, assume any existing elements are OK. 
                    if (oldC != null && newC == null) {
                        invokeStatement(oldInstance, "addLayoutComponent", new Object[]{oldC, names[i]}, out);
                    }
                }
            }
        });
        
        // OverlayLayout
        setPersistenceDelegate(OverlayLayout.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                return new Expression(oldInstance, oldInstance.getClass(), "new", 
                        new Object[]{getPrivateField(oldInstance, OverlayLayout.class, "target", out.getExceptionListener())}); 
            }
        });
        
        // CardLayout
        setPersistenceDelegate(CardLayout.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                CardLayout cl = (CardLayout)oldInstance;
        
                //  Get a pointer to the CardLayout's internal hash table. Given a
                //  child it can be used to look up the "name" supplied by the app. Since we don't
                //  know who the children (or their parent) are, we use this table to
                //  enumerate them.

                Hashtable tab = (Hashtable)getPrivateField(cl, CardLayout.class, "tab", out.getExceptionListener());

                if (tab != null) {
                    int count = tab.size();
                    if (count > 0) {
                        int i = 0;
                        for (Enumeration e=tab.keys(); e.hasMoreElements();) {
                            Component child = (Component)e.nextElement();
                            String  name = (String)tab.get(child);

                            invokeStatement(oldInstance, "addLayoutComponent", 
                                    new Object[]{child, name}, out);
                        }
                    }
                }
            }
        });

        // GridBagLayout
        setPersistenceDelegate(GridBagLayout.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                GridBagLayout gbl = (GridBagLayout)oldInstance;
        
                //  Get a pointer to the GridBagLayout's internal hash table. Given a
                //  child it can be used to look up the GridBagContraints. Since we don't
                //  know who the children (or their parent) are, we use this table to
                //  enumerate them.

                Hashtable comptable = (Hashtable)getPrivateField(gbl, GridBagLayout.class, "comptable", out.getExceptionListener());

                if (comptable != null) {
                    int count = comptable.size();

                    if (count > 0) {
                        int         i = 0;
                        for (Enumeration e=comptable.keys(); e.hasMoreElements();) {
                            Component child = (Component)e.nextElement();
                            GridBagConstraints  gbc = (GridBagConstraints)comptable.get(child);

                            invokeStatement(oldInstance, "addLayoutComponent", 
                                    new Object[]{child, gbc}, out);
                        }
                    }
                }
            }
        });
        
// SWING

    // Models
 
        // DefaultListModel
        setPersistenceDelegate(DefaultListModel.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                // Note, the "size" property will be set here. 
                super.initialize(type, oldInstance, newInstance, out); 
                DefaultListModel m = (DefaultListModel)oldInstance;
                DefaultListModel n = (DefaultListModel)newInstance; 
                for (int i = n.getSize(); i < m.getSize(); i++) {
                    invokeStatement(oldInstance, "add", // Can also use "addElement". 
                            new Object[]{m.getElementAt(i)}, out);
                }
            }
        });

        // DefaultComboBoxModel
        setPersistenceDelegate(DefaultComboBoxModel.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                DefaultComboBoxModel m = (DefaultComboBoxModel)oldInstance;
                for (int i = 0; i < m.getSize(); i++) {
                    invokeStatement(oldInstance, "addElement", new Object[]{m.getElementAt(i)}, out);
                }
            }
        });

    // JComponents
    
        // JComponent (minimumSize, preferredSize & maximumSize). 
        // Note the "size" methods in JComponent calculate default values 
        // when their values are null. In Kestrel the new "isPreferredSizeSet" 
        // family of methods can be used to disambiguate this situation.  
        // We use the private fields here so that the code will work with 
        // Kestrel beta. 
        setPersistenceDelegate(JComponent.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                int statementCount = 0; 
                JComponent c = (JComponent)oldInstance; 
                String[] fieldNames = new String[]{"minimumSize", "preferredSize", "maximumSize"}; 
                for(int i = 0; i < fieldNames.length; i++) { 
                    String name = fieldNames[i]; 
                    Object value = getPrivateField(c, JComponent.class, name, out.getExceptionListener()); 
                    if (value != null) { 
                        // System.out.println("Setting " + name); 
                        invokeStatement(oldInstance, "set" + capitalize(name), new Object[]{value}, out);
                    }
                }
            }
        });
        
        // JTabbedPane
        setPersistenceDelegate(JTabbedPane.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                JTabbedPane p = (JTabbedPane)oldInstance;
                for (int i = 0; i < p.getTabCount(); i++) {
                    invokeStatement(oldInstance, "addTab", 
                                                  new Object[]{
                                                      p.getTitleAt(i),
                                                      p.getIconAt(i),
                                                      p.getComponentAt(i)}, out);
                }
            }
        });

        // JMenu
        // Note that we do not need to state the initialiser for 
        // JMenuItems since the getChildren() method defined in 
        // Container is implemented to return the same results 
        // as getSubElements(). Not so for JMenu apparently. 
        setPersistenceDelegate(JMenu.class, new DefaultPersistenceDelegate() {
            protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
                super.initialize(type, oldInstance, newInstance, out); 
                JMenu m = (JMenu)oldInstance;
                Component[] c = m.getMenuComponents();
                for (int i = 0; i < c.length; i++) {
                    invokeStatement(oldInstance, "add", new Object[]{c[i]}, out);
                }
            }
        });
        
    // Constructors.
    
        registerConstructor(Insets.class, new String[]{"top", "left", "bottom", "right"});
        registerConstructor(Color.class, new String[]{"red", "green", "blue","alpha"});
        registerConstructor(ColorUIResource.class, new String[]{"red", "green", "blue"});
        registerConstructor(Font.class, new String[]{"name", "style", "size"});
        registerConstructor(FontUIResource.class, new String[]{"name", "style", "size"});
        
        registerConstructor(DefaultCellEditor.class, new String[]{"component"});

        // registerConstructor(javax.swing.tree.DefaultTreeModel.class, new String[]{"root"});
        registerConstructor(javax.swing.tree.TreePath.class, new String[]{"path"});

        /*
        This is required because the JSplitPane reveals a private layout class
        called BasicSplitPaneUI$BasicVerticalLayoutManager which changes with 
        the orientation. To avoid the necessity for instantiating it we cause 
        the orientation attribute to get set before the layout manager - that 
        way the layout manager will be changed as a side effect. Unfortunately, 
        the layout property belongs to the superclass and therefore precedes 
        the orientation property. PENDING - we need to allow this kind of 
        modification. For now, put the property in the constructor. 
        */
        registerConstructorWithBadEqual(JSplitPane.class, new String[]{"orientation"});

        registerConstructorWithBadEqual(BoxLayout.class, new String[]{"target", "axis"});

        registerConstructorWithBadEqual(GridBagConstraints.class, 
                new String[]{"gridx", "gridy", "gridwidth", "gridheight", 
                             "weightx", "weighty", 
                             "anchor", "fill", "insets", 
                             "ipadx", "ipady"});

        // Borders
    
        registerConstructorWithBadEqual(BevelBorder.class, new String[]{"bevelType", "highlightOuter", "highlightInner", "shadowOuter", "shadowInner"});
        registerConstructorWithBadEqual(BorderUIResource.BevelBorderUIResource.class, new String[]{"bevelType", "highlightOuter", "highlightInner", "shadowOuter", "shadowInner"});
        registerConstructorWithBadEqual(CompoundBorder.class, new String[]{"outsideBorder", "insideBorder"});
        registerConstructorWithBadEqual(BorderUIResource.CompoundBorderUIResource.class, new String[]{"outsideBorder", "insideBorder"});
        registerConstructorWithBadEqual(EmptyBorder.class, new String[]{"top", "left", "bottom", "right"});
        registerConstructorWithBadEqual(BorderUIResource.EmptyBorderUIResource.class, new String[]{"top", "left", "bottom", "right"});
        registerConstructorWithBadEqual(EtchedBorder.class, new String[]{"etchType", "highlight", "shadow"});
        registerConstructorWithBadEqual(BorderUIResource.EtchedBorderUIResource.class, new String[]{"etchType", "highlight", "shadow"});
        registerConstructorWithBadEqual(LineBorder.class, new String[]{"lineColor", "thickness"});
        registerConstructorWithBadEqual(BorderUIResource.LineBorderUIResource.class, new String[]{"lineColor", "thickness"});
        // Note this should check to see which of "color" and "tileIcon" is non-null.
        registerConstructorWithBadEqual(MatteBorder.class, new String[]{"top", "left", "bottom", "right", "tileIcon"});
        registerConstructorWithBadEqual(BorderUIResource.MatteBorderUIResource.class, new String[]{"top", "left", "bottom", "right", "tileIcon"});
        registerConstructorWithBadEqual(SoftBevelBorder.class, new String[]{"bevelType", "highlightOuter", "highlightInner", "shadowOuter", "shadowInner"});
        registerConstructorWithBadEqual(TitledBorder.class, new String[]{"border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor"});
        registerConstructorWithBadEqual(BorderUIResource.TitledBorderUIResource.class, new String[]{"border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor"});

        registerConstructor(ImageIcon.class, new String[]{"description"});
        
        registerConstructorWithBadEqual(Statement.class, new String[]{"target", "methodName", "arguments"}); 
        registerConstructorWithBadEqual(Expression.class, new String[]{"target", "methodName", "arguments"}); 
        registerConstructorWithBadEqual(EventHandler.class, new String[]{"target", "action", "eventPropertyName", "listenerMethodName"}); 
        
    // Removals
        
        // These properties have platform specific implementations 
        // and should not appear in archives. 
        removeProperty(ImageIcon.class, "image");
        removeProperty(ImageIcon.class, "imageObserver");

        // This property throws an exception when set in JMenu.
        // PENDING: Note we must delete the property from 
        // the superclass even though the superclass's 
        // implementation does not throw an error. 
        // This needs some more thought. 
        removeProperty(JMenu.class, "accelerator");
        removeProperty(JMenuItem.class, "accelerator");
        // This property unconditionally throws a "not implemented" 
        // exception.
        removeProperty(JMenuBar.class, "helpMenu");

        // The color and font properties in Component need special treatment, see above.
        removeProperty(Component.class, "foreground");
        removeProperty(Component.class, "background");
        removeProperty(Component.class, "font"); 


        // The visible property of Component needs special treatment because of Windows.
        removeProperty(Component.class, "visible");
        
        // The size properties in JComponent need special treatment, see above.
        removeProperty(JComponent.class, "minimumSize");
        removeProperty(JComponent.class, "preferredSize");
        removeProperty(JComponent.class, "maximumSize"); 

        // JButton has a layout property which is an OverlayLayout. 
        // The OverlayLayout class has no equals method and 
        // requires a "target" argument to its mondadic constructor. 
        // There is no public getter for the target. 
        // Remove this since buttons do not often have their 
        // layout properties changed. 
        // 
        // Later ... this issue seems to have been resolved in 1.3. 
        // removeProperty(AbstractButton.class, "layout"); 

        // The caret property throws errors when it it set beyond 
        // the extent of the text. We could just set it after the 
        // text, but this is probably not something we want to archive anyway. 
        removeProperty(JTextComponent.class, "caret");
        removeProperty(JTextComponent.class, "caretPosition");

        // The scroll bars in a JScrollPane are dynamic and should not 
        // be archived. The row and columns headers are changed by 
        // components like JTable on "addNotify". 
        removeProperty(JScrollPane.class, "verticalScrollBar");
        removeProperty(JScrollPane.class, "horizontalScrollBar");
        removeProperty(JScrollPane.class, "rowHeader");
        removeProperty(JScrollPane.class, "columnHeader");

        // Renderers need special treatment, since their properties 
        // change during rendering. 
        removeProperty(JTableHeader.class, "defaultRenderer");
        
        removeProperty(JList.class, "cellRenderer");
        removeProperty(JList.class, "selectedIndices");
     
        // The lead and anchor selection indexes are best ignored.
        // Selection is rarely something that should persist from 
        // development to deployment.
        removeProperty(DefaultListSelectionModel.class, "leadSelectionIndex");
        removeProperty(DefaultListSelectionModel.class, "anchorSelectionIndex");

        // Infinite graphs. 
        removeProperty(RectangularShape.class, "frame"); 
        // removeProperty(Rectangle2D.class, "frame"); 
        // removeProperty(Rectangle.class, "frame"); 
        
        removeProperty(Rectangle.class, "bounds");
        removeProperty(Dimension.class, "size");
        removeProperty(Point.class, "location");

        // Testing
        // removeProperty(Date.class, "time");
        // setPrecedence(Date.class, "time", 100);

// Ordering

        // The selection must come after the text itself.
        removeProperty(JComboBox.class, "selectedIndex");

        // The selectionStart must come after the text itself.
        //setPrecedence(JTextComponent.class, "selectionStart", 1);
        //setPrecedence(JTextComponent.class, "selectionEnd", 1);
        removeProperty(JTextComponent.class, "selectionStart");
        removeProperty(JTextComponent.class, "selectionEnd");

// All selection information should come after the JTabbedPane is built
        // setPrecedence(JTabbedPane.class, "model", -4);
        // setPrecedence(JTabbedPane.class, "selectedIndex", 1);
        // setPrecedence(JTabbedPane.class, "selectedComponent", 1);
        removeProperty(JTabbedPane.class, "selectedIndex");
        removeProperty(JTabbedPane.class, "selectedComponent");

        // The "icon" property must come after the "disabledIcon" property.
        // setPrecedence(AbstractButton.class, "icon", 1); 
    }
    
    static void setPersistenceDelegate(Class type, PersistenceDelegate persistenceDelegate) {
        getBeanInfo(type).getBeanDescriptor().setValue("persistenceDelegate", persistenceDelegate); 
    }

    static PersistenceDelegate getPersistenceDelegate(Class type) {        
        if (type == null) { 
            return nullPersistenceDelegate; 
        }
        else if (type.isArray()) {
            return arrayPersistenceDelegate; 
        }
        else if (isPrimitive(type)) {
            return primitivePersistenceDelegate; 
        }
        else if (Proxy.isProxyClass(type)) {
            return proxyPersistenceDelegate; 
        }
        /*
        else if (type.getDeclaringClass() != null) {
            return new DefaultPersistenceDelegate(new String[]{"this$0"}); 
        }
        */
        
        BeanInfo bi = getBeanInfo(type);
        PersistenceDelegate info = (PersistenceDelegate)bi.getBeanDescriptor().getValue("persistenceDelegate");
        
        return (info != null) ? info : defaultPersistenceDelegate;
    }
    
    private static String capitalize(String propertyName) {
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }        

    // The introspector conses up new BeanInfo each time 
    // we call getBeanInfo. To modify BeanInfo we must 
    // maintain BeanInfo instances ourselves. 
    static BeanInfo getBeanInfo(Class type) { 
        if (classToBeanInfo.get(type) == null) { 
            try { 
                BeanInfo info = Introspector.getBeanInfo(type);  
                classToBeanInfo.put(type, info); 
            }
            catch (Throwable e) {e.printStackTrace();}
        }
        return (BeanInfo)classToBeanInfo.get(type); 
    }
    
    // Metadata setters. 
    
    private static PropertyDescriptor getPropertyDescriptor(Class type, String propertyName) { 
        BeanInfo info = getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors(); 
        // System.out.println("Searching for: " + propertyName + " in " + type); 
        for(int i = 0; i < propertyDescriptors.length; i++) { 
            PropertyDescriptor pd  = propertyDescriptors[i];
            if (propertyName.equals(pd.getName())) { 
                return pd; 
            }
        }
        return null; 
    }
    
    private static void setPropertyAttribute(Class type, String property, String attribute, Object value) { 
        PropertyDescriptor pd = getPropertyDescriptor(type, property); 
        if (pd == null) { 
            System.err.println("Warning: property " + property + " is not defined on " + type); 
            return; 
        }
        pd.setValue(attribute, value); 
    }
                
    private static void removeProperty(Class type, String property) { 
        setPropertyAttribute(type, property, "transient", Boolean.TRUE); 
    }

    private static void registerConstructor(Class type, String[] constructor) {
        setPersistenceDelegate(type, new DefaultPersistenceDelegate(constructor));
    }
    
    private static void registerConstructorWithBadEqual(Class type, String[] constructor) {
        setPersistenceDelegate(type, new DefaultPersistenceDelegate(constructor) { 
            protected boolean mutatesTo(Object oldInstance, Object newInstance) { 
                // Copied from PersistenceDelegate
                return (newInstance != null && 
                        oldInstance.getClass() == newInstance.getClass());  
            } 
        });
    }
    
    private static boolean isPrimitive(Class type) {
        return type == Boolean.class || Number.class.isAssignableFrom(type);
    } 
}

