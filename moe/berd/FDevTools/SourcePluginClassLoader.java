package moe.berd.FDevTools;

import java.io.*;
import java.net.*;
import java.util.*;

public class SourcePluginClassLoader extends URLClassLoader
{
	private SourcePluginLoader loader=null;
	private Map<String,Class> classes=new HashMap<>();
	
	public SourcePluginClassLoader(SourcePluginLoader loader,ClassLoader parent,File file) throws MalformedURLException
	{
		super(new URL[]
		{
			file.toURI().toURL()
		},parent);
		this.loader=loader;
	}
	
	protected Class findClass(String name) throws ClassNotFoundException
	{
		return this.findClass(name,true);
	}
	
	protected Class findClass(String name,boolean checkGlobal) throws ClassNotFoundException
	{
		if(!name.startsWith("cn.nukkit.") && !name.startsWith("net.minecraft."))
		{
			Class result=this.classes.get(name);
			if(result==null)
			{
				if(checkGlobal)
				{
					result=this.loader.getClassByName(name);
				}
				if(result==null)
				{
					result=super.findClass(name);
					if(result!=null)
					{
						this.loader.setClass(name,result);
					}
				}
				this.classes.put(name,result);
			}
			return result;
		}
		else
		{
			throw new ClassNotFoundException(name);
		}
	}
	
	public Set getClasses()
	{
		return this.classes.keySet();
	}
}
