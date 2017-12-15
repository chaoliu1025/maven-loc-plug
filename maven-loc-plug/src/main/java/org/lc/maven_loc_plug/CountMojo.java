package org.lc.maven_loc_plug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @ goal count
 * 使用org.apache.maven.plugin-tools 插件 标注可以不用写在注释中
 * @author lc
 *
 */
public class CountMojo extends AbstractMojo{
	private static String[] INCLUDE_DEFAULT = {"java","xml","properties"};
	
	
	/**
	 * @ parameter expression="${project.basedir}"
	 * @ required
	 * @ readonly
	 */
	private File basedir;
	
	/**
	 * @ parameter expression="${project.build.sourceDirectory}"
	 * @ required
	 * @ readonly
	 */
	private File sourceDirectory;
	
	
	/**
	 * @ parameter expression="${project.build.testSourceDirectory}"
	 * @ required
	 * @ readonly
	 */
	private File testSourceDirectory;
	
	/**
	 * @ parameter expression="${project.build.resources}"
	 * @ required
	 * @ readonly
	 */
	private List<Resource> resources;
	
	/**
	 * @ parameter expression="${project.build.testResources}"
	 * @ required
	 * @ readonly
	 */
	private List<Resource> testResources;
	
	/**The file types which will be included for counting.
	 * @ parameter
	 */
	private String[] includes;
	

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(includes == null || includes.length == 0) {
			includes = INCLUDE_DEFAULT;
		}
		
		try {
			countDir(sourceDirectory);
			countDir(testSourceDirectory);
			for (Resource resource:resources) {
				countDir(new File(resource.getDirectory()));
			}
			for (Resource resource:testResources) {
				countDir(new File(resource.getDirectory()));
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to count lines of code",e);
		}
	}
	
	private void countDir(File dir) throws IOException {
		if (! dir.exists()) {
			return;
		}
		List<File> collected = new ArrayList<File>();
		collectFiles(collected, dir);
		int lines = 0;
		for (File sourceFile:collected) {
			lines += countLine(sourceFile);
		}
		
		String path = dir.getAbsolutePath().substring(basedir.getAbsolutePath().length());
		getLog().info(path + ":" + lines + "lines of code in " + collected.size() + "files");
	}
	
	private void collectFiles(List<File> collected,File file) {
		if (file.isFile()) {
			for (String include:includes) {
				if (file.getName().endsWith("."+include)) {
					collected.add(file);
					break;
				}
			}
		} else {
			for (File sub:file.listFiles()) {
				collectFiles(collected, sub);
			}
		}
	}
	
	private int countLine(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int line = 0;
		try {
			while (reader.ready()) {
				line ++;
			}
		} finally {reader.close();}
		
		return line;
	}

}
