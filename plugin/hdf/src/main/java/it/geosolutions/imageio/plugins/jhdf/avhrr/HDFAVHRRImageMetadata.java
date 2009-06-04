/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jhdf.avhrr;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageReader;

import java.io.IOException;
import java.util.HashMap;

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

public class HDFAVHRRImageMetadata extends BaseImageMetadata {
    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_jhdf_avhrr_avhrrImageMetadata_1.0";

    //
    // public static final String[] metadataFormatNames = {
    // nativeMetadataFormatName };

    private final static String driverName = "HDF4";

    private final static String driverDescription = "Hierarchical Data Format Release 4";

    private HashMap<String, String> additionalMetadata;

    private IIOMetadataNode nativeTree;

    public HDFAVHRRImageMetadata(final BaseImageReader reader,
            final int imageIndex) {
        super(reader, imageIndex);
    }

    protected void setMembers(BaseImageReader imageReader) throws IOException {
        super.setMembers(imageReader);
        final int imageIndex = getImageIndex();
        if (imageReader instanceof HDFAVHRRImageReader) {
            HDFAVHRRImageReader reader = (HDFAVHRRImageReader) imageReader;
            setDriverDescription(driverDescription);
            setDriverName(driverName);
            final double scale = reader.getScale(imageIndex);
            if (!Double.isNaN(scale))
                setScales(new Double[] { Double.valueOf(scale) });
            final double offset = reader.getOffset(imageIndex);
            if (!Double.isNaN(offset))
                setOffsets(new Double[] { Double.valueOf(offset) });

            String name = reader.getLongName(imageIndex);
            if (name != null && name.trim().length() > 0) {
                setDatasetName(name);
            }
            final double noData = reader.getFillValue(imageIndex);
            if (!Double.isNaN(noData)) {
                setNoDataValues(new Double[] { Double.valueOf(noData) });
            }

            // TODO: Setting valid range as max min is ok?
            final double[] validRange = reader.getValidRange(imageIndex);
            if (validRange != null && validRange.length == 2
                    && !Double.isNaN(validRange[0])
                    && !Double.isNaN(validRange[1])) {
                setMinimums(new Double[] { Double.valueOf(validRange[0]) });
                setMaximums(new Double[] { Double.valueOf(validRange[1]) });
            }

            // overviews is always 0
            setNumOverviews(new int[] { 0 });
            
            HDFAVHRRImageReader flatReader = (HDFAVHRRImageReader) imageReader;
            final int numAttributes = flatReader.getNumAttributes(imageIndex);
            this.additionalMetadata = new HashMap<String, String>(numAttributes);
            for (int i = 0; i < numAttributes; i++) {
                String attributePair;
                attributePair = flatReader.getAttributeAsString(imageIndex, i);
                final int separatorIndex = attributePair
                        .indexOf(HDFAVHRRImageReader.SEPARATOR);
                final String attributeName = attributePair.substring(0,
                        separatorIndex);
                final String attributeValue = attributePair.substring(
                        separatorIndex + HDFAVHRRImageReader.SEPARATOR.length(),
                        attributePair.length());
                additionalMetadata.put(attributeName, attributeValue);
            }
        }
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format.
     */
    public Node getAsTree(String formatName) {
        if (HDFAVHRRImageMetadata.nativeMetadataFormatName
                .equalsIgnoreCase(formatName))
            return createNativeTree();
        else if (CoreCommonImageMetadata.nativeMetadataFormatName
                .equalsIgnoreCase(formatName))
            return super.createCommonNativeTree();
        throw new IllegalArgumentException(formatName
                + " is not a supported format name");
    }

    private Node createNativeTree() {
        if (this.nativeTree != null)
            return this.nativeTree;
        nativeTree = new IIOMetadataNode(
                HDFAVHRRImageMetadata.nativeMetadataFormatName);

        // ////////////////////////////////////////////////////////////////////
        //
        // DatasetDescriptor
        //
        // ////////////////////////////////////////////////////////////////////
        if (this.additionalMetadata != null) {
            IIOMetadataNode node = new IIOMetadataNode(ATTRIBUTES_NODE);

            for (String key : this.additionalMetadata.keySet()) {
                final String attributeValue = additionalMetadata.get(key);
                node.setAttribute(key, attributeValue);
            }

            nativeTree.appendChild(node);
        }

        return nativeTree;
    }
}