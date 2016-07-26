package moe.berd.FDevTools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import javax.tools.*;

import cn.nukkit.utils.*;
import cn.nukkit.plugin.*;
import cn.nukkit.command.*;

public class Main extends PluginBase
{
	private static Main obj=null;
	
	private JavaCompiler compiler=null;
	
	public static Main getInstance()
	{
		return obj;
	}
	
	public void onEnable()
	{
		File data=new File(this.getDataFolder(),"packed");
		if(!data.isDirectory() && !data.mkdirs())
		{
			this.getLogger().error("Can't create data folder");
		}
		if(obj==null)
		{
			obj=this;
		}
		if(this.checkCompiler()!=null)
		{
			this.getServer().getPluginManager().registerInterface(SourcePluginLoader.class);
			List<String> loaders=new ArrayList<>();
			loaders.add(SourcePluginLoader.class.getName());
			this.getServer().getPluginManager().loadPlugins(new File(this.getServer().getPluginPath()),loaders,true);
			this.getServer().enablePlugins(PluginLoadOrder.STARTUP);
		}
	}
	
	public boolean onCommand(CommandSender sender,Command command,String label,String[] args)
	{
		switch(command.getName())
		{
		case "makeplugin":
			try
			{
				if(args.length<1)
				{
					return false;
				}
				else
				{
					Plugin plugin=this.getServer().getPluginManager().getPlugin(args[0]);
					if(plugin==null)
					{
						sender.sendMessage("[FDevTools] "+TextFormat.RED+"Plugin not exists,check your plugin name");
					}
					else if(plugin.getPluginLoader().getClass()!=SourcePluginLoader.class)
					{
						sender.sendMessage("[FDevTools] "+TextFormat.RED+"Plugin isn't loaded by FDevTools");
					}
					else
					{
						SourcePluginLoader loader=(SourcePluginLoader)plugin.getPluginLoader();
						File dir=loader.getPluginPath(plugin);
						if(dir==null)
						{
							sender.sendMessage("[FDevTools] "+TextFormat.RED+"Can't find plugin source path!");
						}
						else
						{
							File class_file=new File(dir.getAbsolutePath()+"/src_compile");
							File jar_file=new File(this.getDataFolder(),"packed/"+plugin.getName()+"_v"+plugin.getDescription().getVersion()+".jar");
							if(args.length>=2 && args[1].toLowerCase().equals("true"))
							{
								sender.sendMessage("[FDevTools] "+TextFormat.AQUA+"Re-compiling source...");
								if(!loader.compilePlugin(dir,class_file))
								{
									sender.sendMessage("[FDevTools] "+TextFormat.RED+"Can't compile plugin source!");
									break;
								}
							}
							if(jar_file.exists())
							{
								if(!jar_file.isFile())
								{
									sender.sendMessage("[FDevTools] "+TextFormat.RED+"Plugin jar file exists and it's a directory!");
									break;
								}
								sender.sendMessage("[FDevTools] Jar already exists,overriding...");
								if(!jar_file.delete())
								{
									sender.sendMessage("[FDevTools] "+TextFormat.RED+"Can't override jar file!");
									break;
								}
							}
							FileOutputStream file_stream=new FileOutputStream(jar_file);
							Manifest manifest=new Manifest();
							Attributes attr=manifest.getMainAttributes();
							attr.putValue("Manifest-Version","1.0.0");
							attr.putValue("Signature-Version","1.0.0");
							attr.putValue("Created-By","FDevTools v"+this.getDescription().getVersion());
							attr.putValue("Class-Path",plugin.getDescription().getMain());
							JarOutputStream jar_stream=new JarOutputStream(file_stream,manifest);
							jar_stream.setMethod(JarOutputStream.DEFLATED);
							List<File> src_files=listFolder(class_file,"class");
							List<File> res_files=listFolder(dir,"");
							if(src_files!=null && res_files!=null)
							{
								for(File file:src_files)
								{
									String fileName=file.getAbsolutePath().replace("\\","/").replaceFirst(class_file.toString().replace("\\","/")+"/","");
									sender.sendMessage("[FDevTools] Add class file "+fileName+"...");
									FileInputStream fis=new FileInputStream(file);
									jar_stream.putNextEntry(new JarEntry(fileName));
									int length=0;
									byte[] buffer=new byte[4096];
									while((length=fis.read(buffer))!=-1)
									{
										jar_stream.write(buffer,0,length);
									}
									fis.close();
								}
								for(File file:res_files)
								{
									String fileName=file.getAbsolutePath().replace("\\","/").replaceFirst(dir.toString().replace("\\","/")+"/",""),splited=fileName.split("/")[0];
									if(splited.equals("src") || splited.equals(class_file.getName()))
									{
										continue;
									}
									sender.sendMessage("[FDevTools] Add resouces file "+fileName+"...");
									FileInputStream fis=new FileInputStream(file);
									jar_stream.putNextEntry(new JarEntry(fileName));
									int length=0;
									byte[] buffer=new byte[4096];
									while((length=fis.read(buffer))!=-1)
									{
										jar_stream.write(buffer,0,length);
									}
									fis.close();
								}
								jar_stream.close();
								file_stream.close();
								sender.sendMessage("[FDevTools] "+TextFormat.GREEN+"Jar file saved to "+jar_file.toString());
							}
							else
							{
								sender.sendMessage("[FDevTools] "+TextFormat.RED+"Can't list files!");
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				sender.sendMessage("[FDevTools] "+TextFormat.RED+e.getMessage());
			}
			break;
		default:
			return false;
		}
		return true;
	}
	
	public JavaCompiler getCompiler()
	{
		return this.compiler;
	}
	
	@SuppressWarnings("unchecked")
	public JavaCompiler checkCompiler()
	{
		this.compiler=ToolProvider.getSystemJavaCompiler();
		if(this.compiler==null)
		{
			this.getLogger().info(TextFormat.AQUA+"Compiler not found,loading third-party compiler...");
			File jar_file=new File(this.getDataFolder(),"tools.jar");
			if(!jar_file.isFile())
			{
				this.getLogger().error(TextFormat.YELLOW+"Third-party compiler not found,please download and put tools.jar into "+TextFormat.AQUA+this.getDataFolder().toString());
				return null;
			}

			try
			{
				URLClassLoader loader=new URLClassLoader(new URL[]
				{
					jar_file.toURI().toURL()
				});
				Class tools=loader.loadClass("com.sun.tools.javac.api.JavacTool");
				this.compiler=(JavaCompiler)tools.asSubclass(JavaCompiler.class).newInstance();
			}
			catch (Exception e)
			{
				this.getLogger().error("Can't load third-party compiler,please use JDK to launch Nukkit or download tools.jar and put it into data folder!");
				e.printStackTrace();
				return null;
			}
		}
		return this.compiler;
	}
	
	public static List<File> listFolder(File input,String filter)
	{
		List<File> result=new ArrayList<>();
		if(input!=null)
		{
			if(!input.isDirectory())
			{
				if(filter.equals("") || input.toString().endsWith("."+filter))
				{
					result.add(input);
				}
				return result;
			}
			File[] var3=input.listFiles();
			int var4=var3.length;
			for(int var5=0; var5<var4;++var5)
			{
				File f=var3[var5];
				result.addAll(listFolder(f,filter));
			}
		}
		return result;
	}
	
	public static void removeFolder(File input,String filter)
	{
		if(input!=null)
		{
			if(input.isFile())
			{
				if(filter.equals("") || input.toString().endsWith("."+filter))
				{
					input.delete();
				}
			}
			else if(input.isDirectory())
			{
				for(File f:input.listFiles())
				{
					removeFolder(f,filter);
				}
				input.delete();
			}
		}
	}
}
