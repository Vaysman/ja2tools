/* 
 * The MIT License
 *
 * Copyright 2017 starcatter.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package thebob.ja2maptool.util.compositor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import thebob.assetloader.map.core.components.IndexedElement;
import thebob.ja2maptool.model.TileCategoryMapping;
import thebob.ja2maptool.model.TileMapping;
import thebob.ja2maptool.ui.tabs.mapping.tileset.TilesetMappingTabViewModel;
import static thebob.ja2maptool.util.mapping.MappingIO.TILEMAP_FILE_ID;
import thebob.ja2maptool.util.mapping.TileMappingFileData;

/**
 *
 * @author the_bob
 */
public class SnippetIO {

    public static final String SNIPPETLIST_FILE_ID = "SNIPPETLIST";

    public static void saveSnippetList(String fileName, ObservableList<SelectedTiles> snippets) {
	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator('\n');

	Object[] FILE_HEADER = {
	    SNIPPETLIST_FILE_ID,
	    snippets.size()
	};

	try (FileWriter fileWriter = new FileWriter(fileName); CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {
	    csvFilePrinter.printRecord(FILE_HEADER);

	    for (SelectedTiles snippet : snippets) {
		List recordArray = new ArrayList();
		recordArray.add(snippet.getName());
		recordArray.add(encodeSnippet(snippet));
		csvFilePrinter.printRecord(recordArray);
	    }

	    System.out.println("CSV file was created successfully !!!");
	} catch (Exception e) {
	    System.out.println("Error in CsvFileWriter !!!");
	    e.printStackTrace();
	}
    }

    public static ObservableList<SelectedTiles> loadSnippetList(String fileName) {
	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator('\n');
	ObservableList<SelectedTiles> snippets = FXCollections.observableArrayList();
	try (CSVParser csv = CSVParser.parse(new File(fileName), Charset.forName("UTF-8"), csvFileFormat)) {

	    CSVRecord header = csv.iterator().next();

	    String fileTypeId = header.get(0);
	    if (!fileTypeId.equals(SNIPPETLIST_FILE_ID)) {
		throw new RuntimeException("Not a snippet list file!");
	    }

	    int count = Integer.valueOf(header.get(1));

	    for (int i = 0; i < count; i++) {
		CSVRecord snippetRecord = csv.iterator().next();

		String name = snippetRecord.get(0);
		String snippet64 = snippetRecord.get(1);

		SelectedTiles snippet = decodeSnippet(snippet64);
		snippet.setName(name);

		snippets.add(snippet);
	    }
	} catch (IOException ex) {
	    Logger.getLogger(TilesetMappingTabViewModel.class.getName()).log(Level.SEVERE, null, ex);
	}

	return snippets;
    }

    public static SelectedTiles decodeSnippet(String snippet64) {
	byte[] snippetBytes = Base64.getDecoder().decode(snippet64);
	ByteBuffer buffer = ByteBuffer.wrap(snippetBytes);

	int width = buffer.getInt();
	int height = buffer.getInt();
	int tileset = buffer.getInt();

	int layerCount = buffer.getInt();
	IndexedElement[][][] layers = new IndexedElement[layerCount][][];

	for (int i = 0; i < layerCount; i++) {
	    int layerLength = buffer.getInt();
	    layers[i] = new IndexedElement[layerLength][];

	    for (int j = 0; j < layerLength; j++) {
		int tileCount = buffer.getInt();
		layers[i][j] = new IndexedElement[tileCount];

		for (int k = 0; k < tileCount; k++) {
		    int tileType = buffer.getInt();
		    int tileIndex = buffer.getInt();

		    layers[i][j][k] = new IndexedElement(tileType, tileIndex);
		}
	    }
	}

	int size = width * height;
	short[] rooms = new short[size];
	for (int i = 0; i < size; i++) {
	    short room = buffer.getShort();
	    rooms[i] = room;
	}
	
	return new SelectedTiles(tileset, width, height, layers, rooms);
    }

    public static String encodeSnippet(SelectedTiles snippet) {
	int size = snippet.getHeight() * snippet.getWidth();
	int tileBytes = size * snippet.getLayers().length * 16 * 4;

	int estimatedMaxSize = tileBytes * 2;

	ByteBuffer buffer = ByteBuffer.allocate(estimatedMaxSize);
	// header
	buffer.putInt(snippet.getWidth());
	buffer.putInt(snippet.getHeight());
	buffer.putInt(snippet.getTilesetId());
	// layers
	buffer.putInt(snippet.getLayers().length);
	for (IndexedElement[][] layer : snippet.getLayers()) {
	    buffer.putInt(layer.length);
	    for (IndexedElement[] tile : layer) {
		buffer.putInt(tile.length);
		for (IndexedElement element : tile) {
		    buffer.putInt(element.type);
		    buffer.putInt(element.index);
		}
	    }
	}
	// room numbers
	for (short room : snippet.getRoomNumbers()) {
	    buffer.putShort(room);
	}

	buffer.flip();
	byte[] snippetBytes = new byte[buffer.limit()];
	buffer.get(snippetBytes);

	return Base64.getEncoder().encodeToString(snippetBytes);
    }

}
