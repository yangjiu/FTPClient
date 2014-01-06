package view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;

import client.FTPFile;

/** A TableModel to hold File[]. */
class FTPFileTableModel extends AbstractTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -642746198293378998L;
	
	private ArrayList<FTPFile> files = new ArrayList<FTPFile>();
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private String[] columns = {
        "Icon",
        "File",
        "Size",
        "Last Modified",
        "Rights",
        "Owner",
        "Group",
    };

    FTPFileTableModel() {
        
    }

    FTPFileTableModel(ArrayList<FTPFile> files) {
        this.files = files;
    }

    public Object getValueAt(int row, int column) {
        //FTPFile file = files[row];
    	FTPFile file = files.get(row);
        switch (column) {
            case 0:
                /*
                 * Works, but it creates new files every time listing is called (simpler - is called A LOT),
                 * so it's probably not so good for hard drive and system and computer.
                 * (Have to find better way)
                 * 
                File f = new File(file.getFilename());
                if (file.isDirectory()) f.mkdir();
				else
					try {
						f.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                Icon i = fileSystemView.getSystemIcon(f);
                f.delete();
                return i;*/
            	File f = new File(file.getFilename());
            	if (file.isDirectory()) f.mkdir();
            	Icon i = fileSystemView.getSystemIcon(f);
            	f.delete();
            	return i;
            case 1:
                return file.getFilename();
            case 2:
            	return file.getSize();
            case 3:
                return file.getDate();
            case 4:
                return file.getRights();
            case 5:
                return file.getOwner();
            case 6:
                return file.getGroup();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 2:
                return Long.class;
            case 3:
            case 4:
            case 5:
            case 6:
            	return String.class;          
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        //return files.length;
    	return files.size();
    }

    public FTPFile getFile(int row) {
        //return files[row];
    	return files.get(row);
    }

    public void setFiles(ArrayList<FTPFile> files2) {
        this.files = files2;
        fireTableDataChanged();
    }
}