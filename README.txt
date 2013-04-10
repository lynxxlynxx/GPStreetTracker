**********************************************************************************************
*Java Framework zur Bachelorarbeit: Automatisierung der Kartographierung von Straßenschildern*
*Autor:                             Philipp Unger                                            *
*Datum:                             11.03.2013                                               *
*Hochschule:                        Universität Passau                                       *
**********************************************************************************************

GPStreetTracker ist ein Java Framework zur Objekterkennung in Videos.

Es bietet ein Plugin zur farb- und formbasierten Erkennung von Verkehrschildern.

Zur Bildverarbeitung werden dabei auch Methoden aus den OpenCV Bibliotheken verwendet.

Zur Verwendung des Programms muss eine funktionierende OpenCV_2.4.3,
sowie eine JavaCV_0.3 Installation vorliegen.

    Genauere Informationen zur Installation von OpenCV auf Ihrem Betriebssystem befinden
    sich auf der Entwicklerhomepage:

    http://www.opencv.org/

    oder unter:

    http://opencv.willowgarage.com/wiki/InstallGuide

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Funktionsweise des Frameworks:

Das Framework beteht aus 3 Bereichen.

	- Der Videoverarbeitung
	- Den Plugins
	- und der GPS-Verarbeitung

1. Videoverarbeitung:

	Hier werden die einzelnen Frames aus dem Video ausgelesen und nacheinander
	an die Plugins übegeben.

2. Plugins

	In den Plugins, welche alle das gleiche Interface implementieren, findet
	die Objekterkennung statt. Hier können beliebige Algorithmen auf den
	Videoframe zur Objekterkennung angewendet werden.

3. GPS-Verarbeitung

	Hier wird der GPX-Track eingelesen und die erkannten Objekte der Ortskoordinate
	zugewiesen, in der sie aufgetreten sind.
	Danach können die Bilder der erkannten Objekte, sowie ein dazugehöriger Waypoint
	im GPX-Track, gespeichert werden.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Für das Framework wurde bereits ein Plugin zur Straßenschilderkennung, namens
'HoughSignRecognition', entwickelt.

Funktionsweise des Plugins 'HoughSignRecognition':

Die Straßenschilder werden anhand ihrer Farbe und Form erkannt.
Die geschieht im Wesentlichen in 2 Schritten:

1. Bildaufbereitung und Filterung

	In der Bildaufbereitung werden als erstes die Farbintensitätswerte der
	zu untersuchenden Bilder erhöht, um die Farben des Bildes besser auszuprägen.
	Somit heben sich die monochromen Verkehrsschilder noch besser von ihrer Umgebung ab.
	Zudem wird so durch die teilweise Eliminierung von zu hellen und zu dunklen
	Stellen ein Störfaktor ausgeglichen, welcher durch ungleichmäßige Lichtverhältnisse
	entsteht. Dies geschieht über Pixelwert-manipulation im HSV Farbraum.
	Danach werden die aufbereiteten Bilder, mittels eines Schwellwertverfahrens
	in Binärbilder umgewandelt. Das Binärbild stellt somit eine Filtermaske des
	im Schwellwertverfahren angewandten Farbwertes dar.

2. Zusammenführung und Segmentierung

	Nach der Filterung werden die einzelnen Farbmasken (Binärbilder) zu einem Binärbild
	zusammengeführt, welches nun eine Maske aller gefilterten Farben darstellt.
	Anschließend wird das Binärbild, anhand von zusammenhängenden Flächen,
	segmentiert und diese Segmente der Formerkennung übergeben.

3. Formerkennung

	Als letzter Schritt werden die gefundenen Segmente mit Hilfe von Hough-Transformationen
	auf die geometrischen Formen von Straßenschildern untersucht. Um die Erkennungsrate
	hierbei zu steigern, wird auf den Binärbildsegmenten vor der Hough Transformation
	ein Gaußscher Weichzeichner angewendet, um eventuelle Lücken, die durch fehlerhafte
	Farbinformationen der Pixel der Originalbilder zustande gekommen sind, auszugleichen.



Die genaue Funktion und Vorgehensweise kann den Kommentaren im Quellcode entnommen werden.