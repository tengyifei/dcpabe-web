package com.nkdata.gwt.streamer.client.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nkdata.gwt.streamer.client.Streamable;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamerException;
import com.nkdata.gwt.streamer.client.std.ArrayStreamer;
import com.nkdata.gwt.streamer.client.std.EnumStreamer;
import com.nkdata.gwt.streamer.client.std.StructStreamer;

/** 
 * This is a server-side implementation of the class.
 * Client implementation is located in emu package. 
 */
public class StreamerInternal
{
	private static Map<String,Streamer> streamers = new HashMap<String, Streamer>();
	
	public static Streamer createRootStreamer() {
		return new Streamer() {};
	}
	
	/**
	 * Dynamically creates streamers for unknown types.
	 * @param className
	 * @return
	 */
	public synchronized static Streamer createStreamerFor( String className )
	{
		Streamer st = streamers.get( className );
		
		if ( st != null )
			return st;
		
		final Class<?> clazz;
		
		try {
			clazz = Class.forName( className );
		} catch ( Throwable e ) {
			return null;
		}
		
		if ( clazz.isInterface() )
			return null;
		
		if ( clazz.isArray() ) {
			// search for base component type
			/*Class<?> baseType = clazz.getComponentType();
			
			while ( baseType.isArray() )
				baseType = baseType.getComponentType();*/
			
			st = createArrayStreamerFor( clazz );
		} else if ( clazz.isEnum() ) {
			st = createEnumStreamerFor( clazz );
		} else {
			if ( !Streamable.class.isAssignableFrom( clazz ) )
				return null;
			
			st = createStructStreamerFor( clazz );
		}
		
		streamers.put( className , st );
		return st;
	}
		
	
	private static Streamer createStructStreamerFor( final Class<?> clazz )
	{
		return new StructStreamer() {
			private final SortedMap<String,Field> fields;
			private final Constructor<?> init;
			{
				try {
					// search for default constructor
					if ( (clazz.getModifiers() & Modifier.ABSTRACT) == 0 ) {
						// instantiable
						Constructor<?> ini = null;
						try {
							ini = clazz.getConstructor();
						} catch ( Exception ex ) {
							try {
								ini = clazz.getDeclaredConstructor();
							} catch ( Exception ex1 ) {
								new StreamerException( "Class can not be instantiated: "+clazz.getName() );
							}
						}
						init = ini;
						init.setAccessible( true );
					} else
						// abstract
						init = null;
					
					// search for fields
	            	fields = new TreeMap<String,Field>();

	            	{	// if superclass is non-streamable search for fields in all superclasses
	            		Class<?> clType = clazz;
	            		
	            		do {
			            	Field[] ff = clType.getDeclaredFields();
			            	
			            	for ( Field f : ff ) {
			            		// if not static and not transient
			            		if ( (f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0 ) {
			            			fields.put( f.getDeclaringClass().getName()+"::"+f.getName(), f );
			            			f.setAccessible( true );
			            		}
			            	}
			            	
			            	clType = clType.getSuperclass();
	            		} while ( clType != null && !Streamable.class.isAssignableFrom( clType ) );
	            	}
				} catch ( Exception ex ) {
					throw new StreamerException( "Error creating streamer for class "+clazz.getName(), ex );
				}
			}
			
			@Override
			protected Class<?> getTargetClass() {
				return clazz;
			}

			@Override
			protected int getFieldNum() {
				return fields.size();
			}

			@Override
			protected Object createObjectInstance() {
				if ( init != null ) {
					try {
						return init.newInstance();
					} catch ( Exception ex ) {
						throw new StreamerException( "Error instantiating class: "+clazz.getName() );
					}
				} else
					throw new StreamerException( "Error instantiating class: "+clazz.getName() );
			}

			@Override
			protected List<Object> getValues(Object obj) {
				List<Object> l = new ArrayList<Object>(fields.size());
				
				for ( Field f : fields.values() )
					try {
						l.add( f.get(obj) );
					} catch ( Exception ex ) {
						throw new StreamerException( "Can not access field "+f.getName()+" in class: "+clazz.getName() );
					}
				
				return l;
			}

			@Override
			protected void setValues(Object obj, List<Object> values)
			{
				int n = 0;
				for ( Field f : fields.values() ) {
					try {
						f.set(obj, values.get(n) );
					} catch ( Exception ex ) {
						throw new StreamerException( "Can not access field "+f.getName()+" in class: "+clazz.getName() );
					}
					n++;
				}
			}
		};
	}
	
	
	private static Streamer createArrayStreamerFor( final Class<?> clazz )
	{
		return new ArrayStreamer() {
			@Override
			protected Object[] createObjectArrayInstance( int length )
			{
				return (Object[]) Array.newInstance( clazz.getComponentType(), length );
			}
		};
	}
	
	private static Streamer createEnumStreamerFor( final Class<?> clazz )
	{
		return new EnumStreamer() {
			private final Enum<?>[] values;
			{
				try {
					Method m = clazz.getMethod( "values" );
					values = (Enum<?>[]) m.invoke( null );
				} catch ( Exception ex ) {
					throw new StreamerException( "Enum class not found", ex );
				}
			}
			
			@Override
			protected Enum<?> getEnumValueOf( int value ) {
				return values[value];
			}
		};
	}
	
	
	public static String urlEncode( String s ) {
		try {
			return URLEncoder.encode( s, "UTF-8" );
		} catch ( Exception ex ) {
			throw new StreamerException( ex );
		}
	}

	
	public static String urlDecode( String s ) {
		try {
			return URLDecoder.decode( s, "UTF-8" );
		} catch ( Exception ex ) {
			throw new StreamerException( ex );
		}
	}
}
