package copyPaste.copyPaste.processesControl.userRequestEx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import copyPaste.copyPaste.data.OrderCopyPaste;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceDialog;

public class FilesFunctions {

	public static final String OVERRIDE_FILES_AFTER_PROCESS = "Override Files";

	public static final String CREATE_NEW_FILES = "Create New Files";

	public static final String RETRY = "Retry";

	public static final String STOP = "Stop";

	public static final String DELETE_CONFLICT = "Delete Conflicts";
	
	public static final String CANCEL = "CANCEL";

	public static final String[] CLONE_OPTIONS = new String[] {CREATE_NEW_FILES, OVERRIDE_FILES_AFTER_PROCESS };

	public static final String[] CONTINUE_OPTIONS_UNEXPECTED = new String[] { STOP, RETRY };

	public static final String[] CONTINUE_OPTIONS_CONFLICT = new String[] { STOP, RETRY, DELETE_CONFLICT };

	private static final String CLOSE_PAR = ")";

	private static final String OPEN_PAR = "(";



	public HashMap<File, ArrayList<File>> generateUserRequestOutputFiles(ObservableList<File> inputFiles,
			ObservableList<File> outputFolders) {
		HashMap<File, ArrayList<File>> inVsOut = new HashMap<>();
		for (File inFile : inputFiles) {
			ArrayList<File> outFiles = new ArrayList<>();
			for (File outFolder : outputFolders) {
				outFiles.add(new File(outFolder.getAbsolutePath() + File.separator + inFile.getName()));
			}
			inVsOut.put(inFile, outFiles);
		}
		return inVsOut;
	}

	public void generateNewOutputFilesWithoutCoincidences(ArrayList<File> outFilesThatExist,
			ObservableList<OrderCopyPaste> progressList, HashMap<File, ArrayList<File>> inVsOut) {
		for (File outFileThatExist : outFilesThatExist) {
			File newFile = generateNewFile(outFileThatExist);
			inVsOut.values().forEach(replaceCoincidences(outFileThatExist, newFile));
		}
	}

	private File generateNewFile(File outFileThatExist) {
		File newFile = null;
		String absolutePath = outFileThatExist.getAbsolutePath();
		String extension = FilenameUtils.getExtension(outFileThatExist.getName());
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(absolutePath);
		if (extension != null && !extension.isEmpty()) {
			newFile = generateNewFileWithExt(stringBuilder, extension);
		} else {
			newFile = generateNewFileWithoutExt(stringBuilder);
		}
		return newFile;
	}

	public Optional<String> choseOption(String[] options, String cause) {
		ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(options[0], options);
		if(cause!=null) {
			choiceDialog.setTitle("Conflict :"+cause);
		}

		return choiceDialog.showAndWait();
	}
	
	public void choseOption(String[] options, StringBuilder result, String cause) {
		Optional<String> optional = choseOption(options, cause);
		if(optional.isPresent()) {
			result.append(optional.get());
		}else {
			result.append(FilesFunctions.CANCEL);
		}
	}

	public ArrayList<File> checkCoincidences(HashMap<File, ArrayList<File>> inVsOut,
			ObservableList<OrderCopyPaste> progressList) {
		Collection<ArrayList<File>> outFilesss = inVsOut.values();
		Collection<ArrayList<File>> nextFilesss = progressList.stream().map(OrderCopyPaste::getOutputFiles)
				.collect(Collectors.toList());
		return checkCoincidences(outFilesss, nextFilesss);
	}

	public boolean chechAbsolutePaths(File outFile, File nextFile) {
		return nextFile.getAbsolutePath().equals(outFile.getAbsolutePath());
	}

	private File generateNewFileWithoutExt(StringBuilder stringBuilder) {
		File newFile = null;
		boolean generated = false;
		int generation = 0;
		while (!generated) {
			replaceGeneration(generation, stringBuilder);
			try {
				newFile = new File(stringBuilder.toString());
				generated = newFile.createNewFile();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return newFile;
	}

	private File generateNewFileWithExt(StringBuilder stringBuilder, String extension) {
		File newFile = null;
		boolean generated = false;
		int generation = 0;
		while (!generated) {
			replaceGeneration(generation, stringBuilder, extension);
			try {
				newFile = new File(stringBuilder.toString());
				generated = newFile.createNewFile();
			} catch (Exception e) {
				System.out.println(e);
			}
			generation++;
		}
		return newFile;
	}

	private void replaceGeneration(int generation, StringBuilder stringBuilder, String extension) {
		if (generation != 0) {
			replaceAfterGen(generation, stringBuilder);
		} else {
			stringBuilder.replace(stringBuilder.lastIndexOf(extension) - 1, stringBuilder.length(),
					OPEN_PAR + generation + CLOSE_PAR + "." + extension);
		}
	}

	private void replaceAfterGen(int generation, StringBuilder stringBuilder) {
		stringBuilder.replace(stringBuilder.lastIndexOf(OPEN_PAR), stringBuilder.lastIndexOf(CLOSE_PAR) + 1,
				OPEN_PAR + generation + CLOSE_PAR);
	}

	private void replaceGeneration(int generation, StringBuilder stringBuilder) {
		if (generation != 0) {
			replaceAfterGen(generation, stringBuilder);
		} else {
			stringBuilder.append(OPEN_PAR + generation + CLOSE_PAR);
		}
	}

	private Consumer<? super ArrayList<File>> replaceCoincidences(File outFileThatExist, File newFile) {
		return (outFiles) -> {
			outFiles.replaceAll(matchAbsolPath(outFileThatExist, newFile));
		};
	}

	private UnaryOperator<File> matchAbsolPath(File outFileThatExist, File newFile) {
		return (File f) -> {
			if (outFileThatExist.equals(f)) {
				f = newFile;
			}
			return f;
		};
	}

	private ArrayList<File> checkCoincidences(Collection<ArrayList<File>> outFilesArrayArray,
			Collection<ArrayList<File>> nextFiles) {
		ArrayList<File> coincidences = new ArrayList<>();
		for (ArrayList<File> outFiles : outFilesArrayArray) {
			for (File outFile : outFiles) {
				if (outFile.exists() || checkCoincidences(outFile, nextFiles)) {
					coincidences.add(outFile);
				}
			}
		}
		return coincidences;
	}

	private boolean checkCoincidences(File outFile, Collection<ArrayList<File>> nextFilesArrayAray) {
		for (ArrayList<File> nextFiles : nextFilesArrayAray) {
			for (File nextFile : nextFiles) {
				if (chechAbsolutePaths(outFile, nextFile)) {
					return true;
				}
			}
		}
		return false;
	}

}
