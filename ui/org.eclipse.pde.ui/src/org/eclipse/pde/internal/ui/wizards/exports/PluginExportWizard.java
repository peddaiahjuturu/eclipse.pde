package org.eclipse.pde.internal.ui.wizards.exports;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.FragmentBuildScriptGenerator;
import org.eclipse.pde.internal.build.ModelBuildScriptGenerator;
import org.eclipse.pde.internal.build.PluginBuildScriptGenerator;
import org.eclipse.pde.internal.core.TargetPlatform;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.preferences.MainPreferencePage;

/**
 * Insert the type's description here.
 * @see Wizard
 */
public class PluginExportWizard extends BaseExportWizard {
	private static final String KEY_WTITLE = "ExportWizard.Plugin.wtitle";
	private static final String STORE_SECTION = "PluginExportWizard";

	/**
	 * The constructor.
	 */
	public PluginExportWizard() {
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_NEWEXPRJ_WIZ);
		setWindowTitle(PDEPlugin.getResourceString(KEY_WTITLE));
	}

	protected BaseExportWizardPage createPage1() {
		return new PluginExportWizardPage(getSelection());
	}

	protected HashMap createProperties(String destination) {
		HashMap map = new HashMap(4);
		map.put("build.result.folder", destination + Path.SEPARATOR + "build_result");
		map.put("temp.folder", destination + Path.SEPARATOR + "temp");
		map.put("destination.temp.folder", destination + Path.SEPARATOR + "temp");
		map.put("plugin.destination", destination);
		return map;
	}
	
	protected void doExport(
		boolean exportZip,
		boolean exportSource,
		String destination,
		String zipFileName,
		IModel model,
		IProgressMonitor monitor) {

		IPluginModelBase modelBase = (IPluginModelBase) model;
		try {
			String label =
				PDEPlugin.getDefault().getLabelProvider().getObjectText(
					modelBase.getPluginBase());
			monitor.setTaskName(
				PDEPlugin.getResourceString("ExportWizard.exporting") + " " + label);
			monitor.beginTask("", 10);
			makeScript(modelBase);
			monitor.worked(1);
			runScript(
				modelBase,
				destination,
				exportZip,
				exportSource,
				new SubProgressMonitor(monitor, 9));
		} catch (Exception e) {
			if (writer != null && e.getMessage() != null)
				writer.write(e.getMessage() + System.getProperty("line.separator"));
		} finally {
			deleteBuildFile(modelBase);
			monitor.done();
		}

	}
	
	public void deleteBuildFile(IPluginModelBase model) {
		String fileName = MainPreferencePage.getBuildScriptName();
		File file = new File(model.getInstallLocation() + Path.SEPARATOR + fileName);
		if (file.exists())
			file.delete();
	}

	public IDialogSettings getSettingsSection(IDialogSettings master) {
		IDialogSettings setting = master.getSection(STORE_SECTION);
		if (setting == null) {
			setting = master.addNewSection(STORE_SECTION);
		}
		return setting;
	}

	private void makeScript(IPluginModelBase model) throws CoreException {
		ModelBuildScriptGenerator generator = null;
		if (model.isFragmentModel())
			generator = new FragmentBuildScriptGenerator();
		else
			generator = new PluginBuildScriptGenerator();

		generator.setBuildScriptName(MainPreferencePage.getBuildScriptName());
		generator.setScriptTargetLocation(model.getInstallLocation());
		generator.setInstallLocation(model.getInstallLocation());

		IProject project = model.getUnderlyingResource().getProject();
		if (project.hasNature(JavaCore.NATURE_ID)) {
			IPath path =
				JavaCore.create(project).getOutputLocation().removeFirstSegments(1);
			generator.setDevEntries(new String[] { path.toOSString()});
		} else {
			generator.setDevEntries(new String[] { "bin" });
		}

		generator.setPluginPath(TargetPlatform.createPluginPath());

		generator.setModelId(model.getPluginBase().getId());
		generator.generate();
	}

	private void runScript(
		IPluginModelBase model,
		String destination,
		boolean exportZip,
		boolean exportSource,
		IProgressMonitor monitor)
		throws InvocationTargetException, CoreException {
		AntRunner runner = new AntRunner();
		runner.addUserProperties(createProperties(destination));
		runner.setAntHome(model.getInstallLocation());
		runner.setBuildFileLocation(
			model.getInstallLocation()
				+ Path.SEPARATOR
				+ MainPreferencePage.getBuildScriptName());
		runner.addBuildListener("org.eclipse.pde.internal.ui.ant.ExportBuildListener");
		runner.setExecutionTargets(getExecutionTargets(exportZip, exportSource));
		runner.run(monitor);
	}
	
	private String[] getExecutionTargets(boolean exportZip, boolean exportSource) {
		ArrayList targets = new ArrayList();
		if (!exportZip) {
			targets.add("build.update.jar");	
		} else {
			targets.add("build.jars");
			targets.add("gather.bin.parts");
			if (exportSource) {
				targets.add("build.sources");
				targets.add("gather.sources");
			}
		}
		return (String[]) targets.toArray(new String[targets.size()]);
	}
}