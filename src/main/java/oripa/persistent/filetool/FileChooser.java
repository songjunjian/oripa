package oripa.persistent.filetool;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import oripa.ORIPA;
import oripa.exception.UserCanceledException;

/**
 * 
 * @author OUCHI Koji
 * 
 */

public class FileChooser<Data> extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4700305827321319095L;

	public FileChooser() {

		super();
	}

	public FileChooser(final String path) {
		super(path);
		String trimmedPath = replaceExtension(path, "");

		// File file = new File(trimmedPath);
		File file = new File(path);
		this.setSelectedFile(file);

		System.out.println(path);
	}

	/**
	 * don't use this!
	 */
	@Override
	@Deprecated
	public void addChoosableFileFilter(final FileFilter filter) {

	}

	public void addChoosableFileFilter(
			final FileAccessSupportFilter<Data> filter) {
		// TODO Auto-generated method stub
		super.addChoosableFileFilter(filter);
	}

	public String replaceExtension(final String path, final String ext) {

		String path_new;

		path_new = path.replaceAll("\\.\\w+$", "");
		path_new += ext;

		return path_new;
	}

	/**
	 * this method does not change {@code path}.
	 * 
	 * @param path
	 * @param extensions
	 *            ex) ".png"
	 * @return path string with new extension
	 */
	public String correctExtension(final String path, final String[] extensions) {

		String path_new = new String(path);

		boolean isCorrect = false;
		for (int i = 0; i < extensions.length; i++) {
			if (path.endsWith(extensions[i])) {
				isCorrect = true;
				break;
			}
		}

		if (isCorrect == false) {
			path_new = replaceExtension(path_new, extensions[0]);
		}

		return path_new;
	}

	/**
	 * Opens chooser dialog and return saver object for the chosen file. throws
	 * {@link IllegalArgumentException} if user selected a file which has a
	 * extension not supported by the selected filter.
	 * 
	 * @param parent
	 *            parent GUI component
	 * @return saver object.
	 */
	public AbstractSavingAction<Data> getActionForSavingFile(
			final Component parent)
			throws FileChooserCanceledException {

		if (JFileChooser.APPROVE_OPTION != this.showSaveDialog(parent)) {
			throw new FileChooserCanceledException();
		}

		String filePath = null;

		FileFilter rawFilter = this.getFileFilter();
		if (!(rawFilter instanceof FileAccessSupportFilter<?>)) {
			throw new RuntimeException("Wrong Implementation!");
		}
		try {

			@SuppressWarnings("unchecked")
			FileAccessSupportFilter<Data> filter =
					(FileAccessSupportFilter<Data>) (rawFilter);

			String[] extensions = filter.getExtensions();

			filePath = correctExtension(this.getSelectedFile().getPath(),
					extensions);

			if (filePath == null) {
				throw new IllegalArgumentException(
						"wrong extension of selected name");
			}

			File file = new File(filePath);
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(null,
						ORIPA.res.getString("Warning_SameNameFileExist"),
						ORIPA.res.getString("DialogTitle_FileSave"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
					throw new UserCanceledException();
				}
			}

			return filter.getSavingAction().setPath(filePath);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, e.toString(),
					ORIPA.res.getString("Error_FileSaveFailed"),
					JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	/**
	 * Opens chooser dialog and returns loader object for the chosen file.
	 * 
	 * @param parent
	 *            parent GUI component
	 * @return loader object.
	 */
	public AbstractLoadingAction<Data> getActionForLoadingFile(
			final Component parent) {

		if (JFileChooser.APPROVE_OPTION != this.showOpenDialog(parent)) {
			return null;
		}

		FileFilter rawFilter = this.getFileFilter();
		if (!(rawFilter instanceof FileAccessSupportFilter<?>)) {
			throw new RuntimeException("Wrong Implementation!");
		}

		try {
			String filePath = this.getSelectedFile().getPath();
			@SuppressWarnings("unchecked")
			FileAccessSupportFilter<Data> filter =
					(FileAccessSupportFilter<Data>) (this.getFileFilter());

			return filter.getLoadingAction().setPath(filePath);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString(),
					ORIPA.res.getString("Error_FileLoadFailed"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// } catch (FileVersionError v_err) {
		// JOptionPane.showMessageDialog(this,
		// "This file is compatible with a new version. "
		// + "Please obtain the latest version of ORIPA",
		// "Failed to load the file", JOptionPane.ERROR_MESSAGE);
		// return null;

	}

	// /*
	// * (non Javadoc)
	// *
	// * @see javax.swing.JFileChooser#getFileFilter()
	// */
	// @SuppressWarnings("unchecked")
	// @Override
	// public FileAccessSupportFilter<Data> getFileFilter() {
	// // TODO 自動生成されたメソッド・スタブ
	// return (FileAccessSupportFilter<Data>) super.getFileFilter();
	// }

}