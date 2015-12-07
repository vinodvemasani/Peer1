import java.io.*;

/**
  * A class to take a file and split it into smaller files 
  * (for example, for fitting a file on floppy disks).
  * <P>To execute the program to split files apart, pass the -split
  * option and specify a filename:<BR>
  * <I>java FileSplitter -split bigFile.zip</I>
  * <P>To join a file back together, specify the -join flag and
  * give the base filename:<BR>
  * <I>java FileSplitter -join bigFile.zip</I><BR>
  * @author Keith Trnka
  */
public class FileSplitter
	{
	/** 
	  * a constant representing the size of a chunk that would go on a floppy disk.
	  * 1.4 MB is used instead of 1.44 MB so that it isn't too close to the limit.
	  */
	public static final long floppySize = (long)(1 * 1024 * 1024);
	
	/** the maximum size of each file "chunk" generated, in bytes */
	public static long chunkSize = floppySize;
	
		
	/**
	  * split the file specified by filename into pieces, each of size
	  * chunkSize except for the last one, which may be smaller
	  */
	public static int split(String filename, String outputDirectory) throws FileNotFoundException, IOException
		{
		// open the file
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
		// get the file length
		File f = new File(filename);
		long fileSize = f.length();
		//Home.getInstance().txtText.append(outputDirectory+f.getName());
		// loop for each full chunk
		int subfile;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputDirectory+"\\"+"info.ase", "UTF-8");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (subfile = 0; subfile < fileSize / chunkSize; subfile++)
			{
			// open the output file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputDirectory+"\\"+f.getName() + "." + subfile));
			
			writer.print(subfile+"\t");
			   
		    
			// write the right amount of bytes
			for (int currentByte = 0; currentByte < chunkSize; currentByte++)
				{
				// load one byte from the input file and write it to the output file
				out.write(in.read());
				}
				
			// close the file
			out.close();
			}
		
		// loop for the last chunk (which may be smaller than the chunk size)
		if (fileSize != chunkSize * (subfile - 1))
			{
			// open the output file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputDirectory+"\\"+f.getName() + "." + subfile));
			writer.print(subfile+"\t");
			// write the rest of the file
			int b;
			while ((b = in.read()) != -1)
				out.write(b);
				
			// close the file
			out.close();			
			}
		
		// close the file
		in.close();
		writer.close();
		if(subfile==0)
			return 1;
		return subfile+1;
		}
		
	/**
	  * list all files in the directory specified by the baseFilename
	  * , find out how many parts there are, and then concatenate them
	  * together to create a file with the filename <I>baseFilename</I>.
	  */
	public static void join(String baseFilename) throws IOException
		{
		String directory = "D:\\xampp\\htdocs\\Bittorrent\\Files\\"+stripExtension(baseFilename);
		int numberParts = getNumberParts(directory+"\\"+baseFilename);

		// now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\Rakesh\\IdeaProjects\\Peer1\\downloads\\"+baseFilename));
		for (int part = 0; part < numberParts; part++)
			{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(directory+"\\"+baseFilename + "." + part));

			int b;
			while ( (b = in.read()) != -1 )
				out.write(b);

			in.close();
			}
		out.close();
		//Home.getInstance().txtText.append("File Created: C:\\BitTorrent\\Downloads\\"+baseFilename);
		}
	
	/**
	  * find out how many chunks there are to the base filename
	  */
	private static int getNumberParts(String baseFilename) throws IOException
		{
		// list all files in the same directory
		File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
		final String justFilename = new File(baseFilename).getName();
		String[] matchingFiles = directory.list(new FilenameFilter()
			{
			public boolean accept(File dir, String name)
				{
				return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.\\d+$");
				}
			});
		return matchingFiles.length;
		}
	static String stripExtension (String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }
	}
