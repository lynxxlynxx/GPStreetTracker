package tracking.model;

import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import tracking.model.plugins.StreetObject;

/**
 * Process the gps data. Save data from street objects and gpx files.
 * 
 * @author Philipp
 */
public class GPSProcessor extends Observable {

    private LinkedList<StreetObject> streetObjects;

    private Calendar cal = Calendar.getInstance();
    private Date date;
    private int index = 0;

    /**
     * Constructor for the {@link GPSProcessor}.
     * 
     * @param streetObjects
     *            the {@link LinkedList} of found {@link StreetObject}.
     */
    public GPSProcessor(LinkedList<StreetObject> streetObjects) {
        this.streetObjects = streetObjects;
    }

    /**
     * Save Method. Save the {@link IplImage}s of the {@link StreetObject}s as JPEG in a specified
     * folder and as wapoint into the gpx file.
     * 
     * @param gpx
     *            the gpx file.
     * @param location
     *            the specified folder.
     * @param name
     *            the name of the images and waypoints.
     * @param streetObjects
     *            list of booleans shows which of the {@link StreetObject} are selected to be saved.
     */
    public void save(File gpx, File location, String name,
            boolean[] streetObjects) {

        // Read the given gpx file
        try {
            Document doc;

            try {
                doc = new SAXBuilder().build(gpx);
            } catch (JDOMParseException e) {
                error("Gpx file is no correct XML format.");
                return;
            }

            Element root = doc.getRootElement();

            // is there only one track?
            int trksize = root.getChildren("trk").size();
            if (trksize == 0) {
                error("No track in gpx file.");
                return;
            } else if (trksize > 1) {
                error("More than one track in gpx file.\nMultiple tracks cannot be synchronized with the found Street Objects.");
                return;
            }

            // is there only one track segment in the track?
            Element trk = root.getChild("trk");
            int trksegsize = trk.getChildren("trkseg").size();
            if (trksegsize == 0) {
                error("Track does not contain track segments.");
                return;
            } else if (trksegsize > 1) {
                error("More than one track segment in gpx file.\nMultiple tracks cannot be synchronized with the found Street Objects.");
                return;
            }

            // is the maximum timestamp from the street objects in range of the trackpoints
            Element trkseg = trk.getChild("trkseg");
            int trkptsize = trkseg.getChildren("trkpt").size();
            if (trkptsize == 0) {
                error("No track points in gpx file");
                return;
            } else if (trkptsize < getMaxTimestampInt(streetObjects)) {
                error("Trackpoint data does not fit video.");
                return;
            }

            double[][] latlon = new double[trkptsize][2];
            // iterate over all trackpoints and save lat and lon value into latlon array.
            Iterator<Element> listtrkpt = trkseg.getChildren("trkpt")
                    .iterator();
            int counter = 0;
            while (listtrkpt.hasNext()) {
                Element trkpt = (Element) listtrkpt.next();
                try {
                    latlon[counter][0] = Double.parseDouble(trkpt.getAttribute(
                            "lat").getValue());
                    latlon[counter][1] = Double.parseDouble(trkpt.getAttribute(
                            "lon").getValue());

                } catch (NumberFormatException | NullPointerException e) {
                    // If something goes wrong, show an error message to the user.
                    error("Wrong data format for trackpoint attributes. Must be <trkpt lat=\"double\" lon=\"double.\"></trkpt>");
                    return;
                }
                counter++;
            }

            // clone document file to add waypoints. IS NOT NECESSARY!!!
            // Document outDoc = doc.clone();
            // Element outRoot = doc.getRootElement();

            date = cal.getTime();
            index = 0;

            boolean objectsWithoutTimestamp = false;

            for (int i = 0; i < this.streetObjects.size(); i++) {
                if (streetObjects[i]) {

                    StreetObject obj = this.streetObjects.get(i);

                    String objName = getNameString(obj, name);

                    cvSaveImage(location + System.getProperty("file.separator")
                            + objName, obj.getImage());

                    Element waypoint = new Element("wpt");

                    int timestamp = obj.getTimestampInt();
                    if (timestamp > -1) {
                        timestamp = timestamp == 0 ? 0 : timestamp - 1;// Timestamps exist from
                                                                       // second 1 on. If a
                                                                       // StreetObject is detected
                                                                       // in second 0, take the
                                                                       // first second timestamp.
                        waypoint.setAttribute("lat", "" + latlon[timestamp][0]);
                        waypoint.setAttribute("lon", "" + latlon[timestamp][1]);

                        // set the name of the waypoint
                        Element wayName = new Element("name");
                        wayName.addContent(objName);
                        waypoint.addContent(wayName);

                    } else {
                        objectsWithoutTimestamp = true;
                        System.err.println("Timestamp is < 0 in Plugin "
                                + obj.getPluginName());
                    }
                    root.addContent(0, waypoint);
                } // end if: selected street objects
            } // end for loop: street objects

            if (objectsWithoutTimestamp) {
                error("One or more street objects have no timestamp. These objects can not be saved. All other objects will be saved anyway.");
            }

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream outStream = new FileOutputStream(location
                    + System.getProperty("file.separator") + gpx.getName());
            outputter.output(doc, outStream);
            outStream.close();

        } catch (JDOMException | IOException e) {

            System.err.println("ERROR in JDOM or IOException");
            e.printStackTrace();
            error("Error while reading file. Try again.");
        }

        System.out.println("Saving successfull");

        // Give success message to the observer.
        String message = "Saving successfull!";
        Object[] arg1 = { 1, message };
        setChanged();
        notifyObservers(arg1);
    }

    /**
     * Replace the "regexes" in name with attributes from the {@link StreetObject}.
     * 
     * @param obj
     *            the {@link StreetObject}
     * @param name
     *            a {@link String} with regexes
     * @return a {@link String}, the regexes are replaced with attributes from the
     *         {@link StreetObject}
     */
    private String getNameString(StreetObject obj, String name) {
        boolean hasIndex = false;
        String result = "";
        String[] splitname = name.split("#");

        for (int j = 0; j < splitname.length; j++) {
            if (splitname[j].equals("date")) {
                String d = date.toString();
                result += d.replace(":", "-");

            } else if (splitname[j].equals("name")) {
                result += obj.getPluginName();

            } else if (splitname[j].equals("index")) {
                hasIndex = true;
                result += index;

            } else if (splitname[j].equals("type")) {
                result += obj.getType().toString();

            } else if (splitname[j].equals("frame")) {
                result += obj.getFrame();
            } else {
                result += splitname[j];
            }
        }

        if (!hasIndex) {
            result += "" + index;
        }
        result += ".jpg";
        index++;
        return result;
    }

    /**
     * Return the maximum timestamp of the chosen street objects
     * 
     * @param takeObject
     *            boolean array of the chosen street objects
     * @return the maximum timestam as double
     */
    private double getMaxTimestampInt(boolean[] takeObject) {
        double maxTimestamp = 0;
        for (int i = 0; i < streetObjects.size(); i++) {
            if (takeObject[i]) {
                maxTimestamp = Math.max(streetObjects.get(i)
                        .getTimestampDouble(), maxTimestamp);
            }
        }
        return (int) (maxTimestamp / (double) 1000);
    }

    /**
     * Give an {@link Object}[] to the Observer. [0] is the error code 0 and [1] is the error
     * message. Error message is shown as JOptionPane message dialog
     * 
     * @param text
     *            error message.
     */
    private void error(String text) {
        Object[] arg1 = new Object[2];

        arg1[0] = 0; // 0 is error message
        arg1[1] = text; // the error string
        setChanged();
        notifyObservers(arg1);
    }

    /**
     * Return the list of street objects.
     * 
     * @return a {@link LinkedList}<{@link StreetObject}>.
     */
    public LinkedList<StreetObject> getStreetObjects() {
        return streetObjects;
    }
}
