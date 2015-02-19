package com.example.jonas.mmci1_learning;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity {
    private final String ANSWER = "ANTWORT";
    private final String NEXT = "WEITER";
    private Button nextButton;
    private TextView questText, ansText, proText, knownText, overallKnownText;
    private RadioButton randBox, knownBox, standardBox;
    private float pro = 0.0f;
    private SeekBar knownBar;
    private int rating;

    private String[] questions;
    private String[] answers;
    private int countQuest = 1;
    private int countAnsw = 0;
    private int countRand = 0;
    private int countId = 0;
    private int countKnownList = 0;
    private static final Random rand = new Random();
    private boolean wasKnown = false;
    private List<Integer> knownList;

    private DatabaseManager dbManager;
    private SharedPreferences prefs;
    private final String MODE_KEY = "MODE", COUNTER_KEY = "COUNTER_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextButton = (Button) findViewById(R.id.button);
        questText = (TextView) findViewById(R.id.questText);
        ansText = (TextView) findViewById(R.id.ansText);
        proText = (TextView) findViewById(R.id.proText);
        knownText = (TextView) findViewById(R.id.knownText);
        overallKnownText = (TextView) findViewById(R.id.overallKnown);
        randBox = (RadioButton) findViewById(R.id.randBox);
        knownBox = (RadioButton) findViewById(R.id.knownBox);
        standardBox = (RadioButton) findViewById(R.id.standardBox);
        knownBar = (SeekBar) findViewById(R.id.knownBar);

        randBox.setEnabled(false);
        knownBox.setEnabled(false);
        standardBox.setEnabled(false);
        knownBar.setEnabled(false);

        prepareQuestions();
        prepareAnswers();

        prefs = this.getSharedPreferences("com.example.jonas.mmci1_learning", Context.MODE_PRIVATE);

        pro = (100 / questions.length * countId);
        proText.setText("Frage " + ((countId % questions.length) + 1) + "/" + +questions.length + "\t\t" + String.format("%.1f", pro) + " %");

        questText.setText(questions[0]);
        questText.setTextSize(20);
        Log.d("onCreate", "" + countQuest);
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setRating();
        //setState();
    }

    private void setState() {
        int counter = prefs.getInt(COUNTER_KEY, 0);
        Log.d("setState", "counter " + counter);
        switch (prefs.getString(MODE_KEY, "NORMAL")) {
            case "NORMAL":
                standardBox.setChecked(true);
                countAnsw = counter;
                countQuest = counter;
                break;
            case "ZUFALL":
                randBox.setChecked(true);
                countRand = counter;
                break;
            case "WISSEN":
                knownBox.setChecked(true);
                countKnownList = counter;
                break;
        }
        switchQuestAnsw();
    }


    private void saveState() {
        String sorting = "NORMAL";
        if (randBox.isChecked()) {
            sorting = "ZUFALL";
            //dbManager.createState(countRand, sorting);
            prefs.edit().putInt(COUNTER_KEY, countRand);
        } else if (knownBox.isChecked()) {
            sorting = "WISSEN";
            //dbManager.createState(countKnownList, sorting);
            prefs.edit().putInt(COUNTER_KEY, countKnownList);
        } else {
            //dbManager.createState(countQuest, sorting);
            prefs.edit().putInt(COUNTER_KEY, countQuest).apply();
        }
        prefs.edit().putString(MODE_KEY, sorting).apply();
    }

    private void setKnownRadioListener() {
        knownBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    knownList = dbManager.getIdsWithRatingASC();
                    /*
                    int i = 0;
                    Log.d("knoenList", knownList.get(0)+" "+ knownList.size());
                    if (knownList == null) {
                        for (int a = 0; a< questions.length; a++) {
                            knownList.add(a);
                        }
                    }
                    while (knownList.size() <= questions.length) {
                        if (!knownList.contains(i)) {
                            knownList.add(i);
                        }
                        i++;
                    }
                    */
                    if (knownList == null || knownList.size() < questions.length) {
                        for (int i = 0; i < questions.length; i++) {
                            if (!knownList.contains(i)) {
                                dbManager.createRating(i, 0);
                            }
                        }
                    }
                }
            }
        });
    }

    private void setKnownbarListener() {
        knownBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rating = progress;
                knownText.setText("GEWUSST ZU:\t" + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String action = dbManager.createRating(knownList.get(countKnownList-1), rating); /* @TODO: Check if this can be a problem */
                Log.d("rating", " " + action);
            }
        });
    }

    private void setNextButtonListener() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchQuestAnsw();
            }
        });
    }

    private void switchQuestAnsw() {

        // schaltet die Antwort frei
        if (nextButton.getText().equals(ANSWER)) {
            nextButton.setText(NEXT);
            randBox.setEnabled(true);
            knownBox.setEnabled(true);
            standardBox.setEnabled(true);
            knownBar.setEnabled(true);
            if (!randBox.isChecked()) {
                //proText.setAlpha(255);
            }
            if (answers.length > countAnsw) {
                ansText.setText(answers[countAnsw]);
                countAnsw++;
                countAnsw = countAnsw % questions.length;
            }
            if (randBox.isChecked()) {
                //proText.setAlpha(0);
                countQuest++;
                countQuest = countQuest % questions.length;
            }
            if (knownBox.isChecked()) {
                ansText.setText(answers[knownList.get(countKnownList)]);
                countKnownList++;
                if (countKnownList % questions.length == 0) {
                    knownList = dbManager.getIdsWithRatingASC();
                    countKnownList = 0;
                }
            }
        } else { // schlatet zur nächsten Frage
            nextButton.setText(ANSWER);
            ansText.setText(null);
            randBox.setEnabled(false);
            knownBox.setEnabled(false);
            standardBox.setEnabled(false);
            knownBar.setEnabled(false);
            if (questions.length > countQuest && !randBox.isChecked()) {
                questText.setText(questions[countQuest]);
                countQuest++;
                countQuest = countQuest % questions.length;
            } else if (questions.length >= countQuest && !randBox.isChecked()) {
                Toast.makeText(getApplicationContext(), "Und von Vorne", Toast.LENGTH_SHORT);
                countQuest = 1;
                countAnsw = 0;
                ansText.setText(null);
                questText.setText(questions[0]);
            }
            if (randBox.isChecked()) {
                countRand = rand.nextInt(questions.length);
                countAnsw = countRand;
                countQuest = countRand;
                ansText.setText(null);
                questText.setText(questions[countRand]);
            }
            if (knownBox.isChecked()) {
                questText.setText(questions[knownList.get(countKnownList)]);
                //countKnownList++;
            }
            countId++;
            countId = countId % questions.length;
            setRating();
            pro = (100f / questions.length * (countId));
            proText.setText("Frage " + ((countId % questions.length) + 1) + "/" + +questions.length + "\t\t" + String.format("%.1f", pro) + " %");
        }

    }

    private void setRating() {
        if (!knownBox.isChecked()) {
            rating = dbManager.getRating(countId);
        } else {
            rating = dbManager.getRating(knownList.get(countKnownList));
        }
        knownText.setText("GEWUSST ZU:\t" + rating + "%");
        knownBar.setProgress(rating);
        int overall = dbManager.getOverallKnowledge(questions.length);
        overallKnownText.setText("GESAMTWISSEN:\t" + overall + "%");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up nextButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareQuestions() {
        questions = new String[]{
                "- Provide one example and one disadvantage of capacitive and resistive touch screens. Why aren’t resistive touch screens used anymore in high end smartphones?",
                "- Discuss the cause and effect of the fat finger problem. Discuss possible solutions.",
                "- Explain and compare Zoom Pointing, Virtual Keys, Take-off / Offset Cursor, Cross-Keys, Cross-Lever, 2D-Lever, Precision Handle, Shift, Escape and Bubble Cursor.",
                "- Discuss approaches for reducing the size of buttons / reducing the error rate on touch screen smart phones.",
                "- Explain the Contact Area Model. Discuss its advantages and disadvantages.",
                "- Explain the Projected Centre Model and compare it with the Contact Area Model.",
                "- What are the implications for engineering when applying the Projected Centre Model?",
                "- Describe the cause for the systematic offset reported in http://doi.acm.org/10.1145/2037373.2037395 and describe how this knowledge can be applied.",
                "- Describe the three tested options http://doi.acm.org/10.1145/2208636.2208658 and discuss / interpret the findings of the comparative study.};",
                "- Name and sketch five different physical keyboards. Name one advantage and disadvantage for each of them.",
                "- Name and describe three different approaches for text input on touch screens. Name one advantage and disadvantage for each of them.",
                "- Name and describe five different approaches for the assessment of text input techniques.",
                "- What are the six criteria that can be used to describe keyboards?",
                "- What are the three conceptual advantages of the Dvorak keyboard when compared with the QWERTY keyboard?",
                "- A 12-key keypad suffers from the ambiguity problem. Name, describe and compare the four approaches used to address this issue.",
                "- Why are the keyboard layouts FITALY and OPTI in principle better than QWERTY? Discuss the theoretical background.",
                "- Discuss why most modern smartphones don’t provide support for handwriting (recognition).",
                "- What are the advantages and disadvantages of CROSSING text input techniques when compared with POINT & CLICK?",
                "- Discuss how SWYPE extracts the intended work (based on a concrete example).",
                "- What is a good research question?",
                "- Define external and internal validity.",
                "- Why is there always a trade-off between external and internal validity?",
                "- What is an independent (dependent) variable?",
                "- What is a hypothesis? Explain the term based on a chosen example.",
                "- What is the difference between a within and between subjects design?",
                "- What is counterbalancing? Why is this important? How can it be applied?",
                "- What is the relationship between control and random variables?",
                "- Name three observational methods and describe the difference between them.",
                "- Which expertise / groups are required in a “perfect” mobile human-computer-interaction design team? What are their tasks / how do they individual groups contribute? (see “Functions in a Design Team”)",
                "- What are the basic requirements which have to be fulfilled in a successful design? (see “The Design Hierarchy of Needs”)",
                "- Why should one use design rules?",
                "- Which five categories of design rules exist? Provide one example for each of them. Classify the five categories of design rules according their generality and authority (provide a rationale for each category)?",
                "- Principles of usability (slide 22) should be learned by heart",
                "- What are the benefits of using golden rules? When are they used? How are they used?",
                "- Which information can be found in guidelines / a style guide? Who defines guidelines / a style guide?",
                "- What is a HCI design pattern?",
                "- What are the elements of a typical HCI design pattern (slide 49)?",
                "- What are the advantages and disadvantages of a HCI design pattern?",
                "- Which trend in mobile hci patterns do we currently observe?",
                "- Technology could support head, blink and eye tracking. Discuss the difference between the three options. Discuss the interactions and applications which could be enabled through the three options. Discuss the advantages and disadvantages of the three options (technology required, computing effort, supported interactions, enabled applications, etc.).",
                "- Explain how eye tracking works (in principle).",
                "- What can be tracked through an eye tracker (mention 7 aspects)?",
                "- Explain the midas touch problem.",
                "- How could selection, moving an object, scrolling text and menu commands be enabled by an eye tracker?",
                "- What is the motivation behind eye-gaze interaction? What are the advantages and disadvantages when compared with classic eye movement-based interaction techniques?",
                "- What are the implications of the various levels of accuracy of the available eye tracking technologies on the interaction and user interface design?",
                "- Explain the principle of electrooculography / EEG eye tracker",
                "- Explain saccades and fixations of eye movement and the frequency in which they occur. How to they differ from mouse cursor movements?",
                "- When are smooth pursuits occurring? What is the difference between saccades, fixations and smooth pursuits?",
                "- How could smooth pursuits be used for an interaction technique?",
                "- Discuss the difference between a smartphone and computerized eyewear (e.g. Google Glass) in terms of the interaction design.",
                "- Name and discuss the five most important (in your opinion) technical issue of computerized eyewear.",
                "- What is the vision behind computerized eyewear? ",
                "What is the vision behind the remembrance agent? ",
                "What is “over the shoulder telepresence” (also advantages and disadvantages)? ",
                "What are the core interaction concepts and applications of computerized eyewear?",
                "- Why is project glass or computerized eyewear (potentially) an extension of the self? What are the core visions, concepts and principles (e.g. immediacy, 2 seconds rule, etc)? (see http://dx.doi.org/10.1109/MPRV.2013.35)",
                "- Which privacy issues are introduced trough computerized eyewear?",
                "- Question regarding classification of head-mounted displays (figure on slide 27 should be learnt by heart, and understood). Question regarding the advantages and disadvantages of the different options. Comparison of the different options.",
                "- Explain binocular rivalry and depth of focus issues (monocular head-mounted displays)",
                "- Why isn’t computerized eyewear used in industry (non-military usages)? Discuss the 5 most important issues.",
                "- How could industry (applications, non-military usages) benefit from computerized eyewear? Name a concrete application and discuss the conceptual advantages.",
                "- What is a tacton?",
                "- What is the motivation for a compound tacton? Why are compound tactons rarely used in practice?",
                "- What is the difference between an inertial shaker and a linear actuator? Mention one advantage and one disadvantage for each of the two options.",
                "- Which parameters can be used to define a vibration?",
                "- Why do many smartphones (e.g. Android) provide by default tactile feedback (vibration) when the user is typing?",
                "- Describe how navigational feedback could be provided by 1, 2 or N inertial shakers?",
                "- What are the advantages and disadvantages of using just one inertial shaker to communicate navigation information?",
                "- Mention 3 application areas for a mobile phone with 6 inertial shakers included in the back of the mobile phone.",
                "- What are the advantages and disadvantages of TeslaTouch?",
                "- Mention and discuss 5 core concepts of Nokia Morph",
                "- What are the three most important benefits of flexible or organic user interfaces? Argue why those three are so important (in your opinion).",
                "- Discuss the Gummi System (specific question(s) regarding concept, advantages and disadvantages, interactions)",
                "- Discuss the Kinetic Device (specific question(s) regarding concept, advantages and disadvantages)",
                "- Discuss the PaperPhone system (specific question(s) regarding concept, advantages and disadvantages, interactions)",
                "- Why do we still have paper in our offices / homes / etc.?",
                "- Discuss the Xpaaand system (specific question(s) regarding concept, advantages and disadvantages)",
                "- Discuss shape-changing mobiles, this includes the Morphees concept (specific question(s) regarding concept, advantages and disadvantages)",
                "- What is a Persona?",
                "- What makes a Persona a good Persona?",
                "- What is a Scenario?",
                "- What makes a Scenario a good Scenario?",
                "- What are the benefits of low fidelity Prototypes?",
                "- What are the differences between low and high fidelity Prototypes?",
                "- What is an Activity?",
                "- What is an Intent?",
                "- Name three methods of the activity lifecycle.",
                "- Name two ways for persisting data with Android."

        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        knownBar.setOnSeekBarChangeListener(null);
        knownBox.setOnCheckedChangeListener(null);
        nextButton.setOnClickListener(null);
        dbManager.close();
        //saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d("dBManager", "error happened");
        }
        setKnownbarListener();
        setKnownRadioListener();
        setNextButtonListener();
        //setState();
    }

    private void prepareAnswers() {
        answers = new String[]{"Resistive: Palm, durch die Stylus eingabe kann eine punktuelle Eingabe erfolgen, allerdings kann, da Druck aufgebaut werden muss kein Swipe ausgeführt werden\n" +
                "Capacitive: Iphone, Sehr verlässliche Eingabemethode durch die Swipen oder multitouch Gesten ermöglicht werden, allerdingst ist die Bedienung mit Handschuehn nicht möglich\n" +
                "Resitive Displays werden nicht mehr verwendet, da die Entnahme des Stylus mehr Zeit benötigt. Auch ist damit eine einhändige Bedienung nicht möglich. Multitouchgesten, wie pinch to zoom sind nicht möglich.",
                "Da der Finger eine geringe Eingabeauflösung im Gegensatz zur hohen Ausgabeauflösung eines Displays hat ist dieser nicht Ideal. Das Display ist relativ klein, daher müssen Targets, damit man viele auf dem Display unterbringen muss relativ klein sein. Da die Fingerspitze groß und weich ist verdeckt sie das Target und führt damit zu Okklusion. Das Verfehlen kleiner Targets oder die Auswahl eines falschen sind damit sehr wahrscheinlich. \n" +
                        "Gelöst kann dies durch Offsetpointer oder durch heranzoomen der berührten Fläche werden.",
                "Zoom Pointing: Dabei wird eine Boundingbox mit dem Finger über dem target gezeichnet, in diese wird dann hereingezoomt. Das erleichtert die Selektion ändert aber den Displaykontext.\n" +
                        "Virtual Keys: Hierbei wird durch Tippen auf die region des Targets ein Fadenkreuz grob positioniert. Anhand von Buttons an einer fixen Position kann das Fadenkreuz exakt über dem Target positioniert werden. Dies geht sehr schnell, allerdings hat man dabei einen Offset zwischen Target und Buttons\n" +
                        "Take-off / Offset Curser: Das ist die Softwareversion eines Stylus, dabei wird oberhalb des berührenden Fingers ein Pointer gezeichnet mit dem man nun Über das Target fahren kann. So wird Okklusion vermieden. Das Verfahren dauert allerdings länger als eine normale Berührung und ist am unteren Bildschirmrand nicht möglich.\n" +
                        "Cross-Keys: Ist eine verbesserte Version des Virtiual Keys, denn die Buttons sitzen nun neben dem Target. Ist das Fadenkreuz positioniert kann es durch tippen auf das Zentrum aktiviert werden. Dies spart Zeit und der Offset ist nicht mehr so groß.\n" +
                        "Cross-Lever: Am Fadenkreuz befinden sich zwei Griffe, durch deren erhöhtem Abstand zum Target kann eine feinere Justierung ermöglicht werden, benötigt allerdings dafür mehr Zeit. Es wird dabei nicht gezoomt, dadurch bleibt die Displayinformation gleich.\n" +
                        "2D-Lever: Nur noch ein Griff, was die Positionierung vereinfacht und beschleunigt. Damit ist eine hohe Präzision möglich. \n" +
                        "Precision Handle: Erhöhte Präzison, da nun das Pivot auch gedreht werden kann. \n" +
                        "Shift: Die brührte Fläsche wird in einer Lupe über dem Finger dargestellt, darin befindet sich nun ein Curser der über das Target bewegt werden kann. Diese Technik hat damit nicht die Probleme des Offset-Curser und des Take-Off. Allerdings muss entschieden werden ab wann die Lupe angezeigt werden soll, wie z.B. nach einer Dwelltime. \n" +
                        "Escape: Kleine nah beieinander liegende Targets zeigen in unterschiedliche Richtungen. Will man ein Target wählen so muss man in dessen angezeigte Richtung swipen. Die Targets ändern ihr visuelles Aussehen. \n" +
                        "Bubble Curser:  Der Curser hat eine große Fläche und das sich näher am Zentrum befindliche Target wird gehighlighted. ",
                "Ein Display ist 2D und ein Finger 6D (2D Position + 3D Rotation + Druck) damit ist ein Finger ein schlecht gemapptes Eingabeinstrument, wobei Nutzer ein flasches Menstales Modell bekommen. Beim Kontaktflächenmodell ist den Nutzern bewusst dass sie mit einer Fläche das Display berühren. Unteranderem bedingt durch die Parallaxe entsteht dabei ein Offset von durchschnittlich 4mm. Kompensiert wird dies durch die Verschiebung des Tatsächlich berührten Punktes um diesen Abstand. Das Projected Center Model weist im Schnitt einen geringeren Offset auf. Dadurch kann die Trefferqoute und die Fehlerrate verbessert werden, was dazu führt, dass kleinere Targets bzw. kleiner Displays wiederum mit herkömmlicher Treffer- und Fehlerrate genutzt werden können. ",
                "Der Finger berührt das Display nicht in genau einem Punkt sondern mit einer Fläche, seiner Unterseite. Da aber nur die Oberseite zu sehen ist muss der Nutzer den selektierten Punkt  abschätzen, was zu einem Offset von in etwa 4mm führt. Auch ist die Parallaxe ein Problem bei diesem Model. Vorteil an diesem ist, dass den Nutzern bekannt ist wie ein capacitive Touchscreen funktioniert und somit ein an sich richtiges mentales Modell hat. ",
                "Hierbei schaut sich der Nutzer seinen Finger von Oben an und positioniert ihn über dem Target und entscheidet dann ob, durch z.B. rollen oder drehen weitere Anpassungen nötig sind. Diese Modell verursacht einen Offset von etwa 1,6mm und ist damit um das 2.6 fache genauer als das Kontaktflächenmodell. ",
                "Da es um das 2,6 fache genauer ist, kann ein Target um diesen Faktor verkleinert werden ohne dass sich die Genauigkeit von 95% ändert. Das bedeutet, dass 2.6 mal mehr Target auf dem Display positioniert werden können oder das Display um mehr als die hälfte verkleinert werden kann. ",
                "Der Offset befindet sich immer in der Richtung des berührenden Daumens, dies gilt für Rechts- sowie für Linkshänder. Nur sind dabei die Richtungen gespiegelt. Dadurch kann eine Ausgleichsfunktion geschrieben werden, die den Offset korrigiert und die Fehlerrate somit minimiert. ",
                "Drei verschiedene Ansätze zur Verschiebung der Touchevents:\n" +
                        "No Shift: Nur die tatsächlich gefundenen Touchdaten werden verwendet\n" +
                        "Native Shift: Native Funktion von Android, welche die Touchkoordinaten um 10dp nach oben verschiebt.\n" +
                        "Adaptive Shift: Eine Ausgleichsfunktion wird auf die Touchkoordinaten angewendet und verschiebt diese immer in Richtung Mitte der Buttons. \n" +
                        "→ Bereits de Native Shift verbessert die Treffer- und Fehlerrate im Vergleich zu No Shift. Allerdings ist eine noch größere Verbesserung durch den Adaptive Shift möglich. \n" +
                        "Keylabel Shift: Ausgehend von der Vermutung, dass Nutzer immer versuchen das Label einer Taste zu drücken werden diese nach oben verschoben. In der Praxis lässt sich dadurch allerdings keine messbaren Verbesserungen erkennen. \n" +
                        "Touchposition durch Dot anzeigen: Dies kann die Fehlerrate verbessern, weil es die Nutzer dazu bewegt genauer zu drücken bzw. sie lernen wie sie eine Taste genauer treffen. Allerdings benötigt die mehr Zeit. ",
                "QWERTY: Ursprünglich für Schreibmaschienen gedacht und nicht für schnelle Texteingaben. Allerdings ist sie sehr bekannt und am weitesten verbreitet\n" +
                        "AZERTY: \n" +
                        "Dvorak: Rechte Hand wird etwas mehr beansprucht, aber es wird versucht immer zwischen den Händen zu alternieren. Die häufigsten Buchstaben sind in am leichtesten zu erreichen. Layout für unterschiedliche Sprachen anders.\n" +
                        "5Key-Pager: Die fünf tasten brauchen nicht viel Platz, somit kann das Gerät sehr klein sein. Die Technik ist allerdings sehr langsam. \n" +
                        "12 Key Keypad: Längere Lernphase, danach aber schneller als 5Key.\n" +
                        "T15: Mehr Tasten, allerdings ungewohntes Layout\n" +
                        "Chording (Twiddler): Meherer Tasten werden gleichzeitig gedrückt, dadurch kann ein Keyboard mit 101 verschiedenen Tasten emuliert werden. Benötigt eine sehr lange Lernphase, dann aber extrem schnell.",
                "Crossing: Ohne Absetzten kann ein Wort geschrieben werde, das spart Zeit im Vergleich zu Point and Click\n" +
                        "Point and Click: Bekannte Eingabe, aber etwas langsam und mit hoher Fehlerwahrscheinlichkeit behaftet\n" +
                        "Handwriting: Absolut kein Lernaufwand, allerdings schwierig zu erkennen, vor allem den Anfang und das Ende eines Buchstabens. Jede Person hat auch eine andere Handschrift",
                "Geschwindigkeit: Words per minute, Characters per minute\n" +
                        "Effizienz: Keystrokes per Char, Metric for writing/crossing\n" +
                        "Komplexität der Eingabe\n" +
                        "Sichtbarkeit des gerade geschriebenen Wortes\n" +
                        "Okklusion",
                "Key Size\n" +
                        "Key layout\n" +
                        "Key Shape\n" +
                        "Number of Keys\n" +
                        "Activation Force\n" +
                        "Feedback",
                "Die häufigsten Tasten sind am leichtesten und die seltensten am schlechtesten zu erreichen. Es wird versucht immer zwischen den Händen abzuwechseln, wobei aber die rechte, da mehr Rechtshänder, stärker belastet werden sollte.",
                "Da weniger Tasten als mögliche Buchstaben vorhanden sind, ist ein Tastendruck nicht mehr \teindeutig und muss unterschieden werden.\n" +
                        "Chording: Tastenkombinationen ergeben eindeutige Zeichen oder Kommandos. Hoher Lernaufwand danach aber schnell\n" +
                        "Multi-Tab: Leicht zu erlernen, man sieht auch immer was man eben schreibt, allerdings benötigt es durch das mehrfache Tippen viel Zeit\n" +
                        "Wörterbuch: geht sehr schnell für Wörter die im Wörterbuch vorhanden sind, für andere allerdings nicht. Man sieht auch nicht immer gleich was man schreibt\n" +
                        "Wahrscheinlichkeit: Hierbei werden die Präfixe bestimmter Buchstabenwahrscheinlichkeiten abgeglichen. Vorteil ist dass man kein Wörterbuch speichern braucht. Wenn die Wahrscheinlichkeiten eines Wortes abweichen dauert es recht lange. ",
                "Da das Fits' Law besagt, dass der Abstand eines Targets möglichst klein und dessen Größe \tmöglichst groß sein soll. Da bei qwerty die häufigen Tasten aber nicht neben einader liegen,\t \twie bei FITALY und OPTY. Daher sollten diese nach einer Eingewöhnungsphase viel schneller \tsein.",
                "Da jede Person eine andere Handschrift hat und man somit keine Standar „Buchstaben \tPattern“ zum erkennen hinterlegen kann. Der Algorithmus zur Erkennung ist also sehr \t\tkomplex. Des Weiteren ist es nicht einfach den Anfang oder das Ende eines Buchstaben zu \t\terkennen. Handschriftlich verbindet man oft Buchstaben miteinander, das wäre so nur schlecht \tumsetzbar, was dazu führt, dass man Wörter Buchstabe für Buchstabe eingeben muss, was eine \tlänger Zeit in Anspruch nimmt. Außerdem werden heutige Smartphone hauptsächlich mit dem \tFinger bedient was auch keine natürliche Handschrift ermöglicht. Nur manche bieten einen \tStift als zusätzliche Eingabetechnik mit an. ",
                "Eine durchgehende Bewegung um ein Wort zu schreiben ist viel einfacher und benötigt \tweniger zeit, als ständig den Finger wieder zu haben und ab zu setzen. Auch müssen die \tBuchstaben nicht exakt getroffen werden. Ein Nachteil ist dass man nicht sieht was man aktuell \tschreibt. Wenn die Worte beim Crossing nicht bekannt sind, ist die Point and Click Technik, \tmindestens gleich schnell. Point and Click ist eine allseits bekannte Eingabetechnik und muss \tnicht erlernt werden. Durch Einsatz beider Daumen kann man ebenso hohe Geschwindigkeiten \terreichen. \n",
                "Schreibt man das Wort QUICK, so wischt man über die Buchstaben QwertzUICK. Anhand eines \tWörterbuchs wird nun ermittelt, dass das Ende des Wortes ICK und der Anfang Q ist. Nun wird \tnach den am häufigsten vorkommenden Buchstaben dazwischen geschaut, das ist in diesem \tFall U. Möglich ist auch die swipe Geschwindigkeit zu analysieren, denn über zum Wort \tgehörenden Buchstaben wird länger verweilt. \n",
                "Sie sollte sehr genau angeben was alles untersucht werden soll und sollte im Bezug auf ein vergleichbares System oder auf Alternativen Bezug nehmen. „Erhöht sich die WPM durch das neue Verfahren im Vergleich zum Alten nachdem man eine Stunde Einarbeitungszeit hatte?“",
                "Gibt an in wie weit gemessene Ergebnisse generalisiert auf die reale Welt übertragbar sind.\n" +
                        "Gibt an ob das was gemessen werden soll auch gemessen wird",
                "Sie beeinflussen sich immer gegenseitig, denn je genauer die Testumgebung die Realität wieder gibt, desto mehr unkontrollierte Einflüsse gibt es, die das Ergebnis beeinflussen können. Allerdings ist es nicht immer sinnvoll und möglich alle Einflüsse zu kontrollieren. Daher muss ein passender Kompromiss gefunden werden. ",
                "Testbedingungen die beeinflusst werden können, haben mehrere Levels (auditiv, taktil, keine)\n" +
                        "Sind gemessene Daten, wie Zeit, Fehlerraten usw. hängen aber auch von den unabhängigen Variablen ab",
                "Sind Vorhersagen die in Bezug auf die abhängigen und unabhängigen Variablen für den \t\tAusgang des Experiments getroffen werden. Wie z.B. dass sich die WPM im Vergleich zum alten \tSystem erhöhen wird wenn ein Shift eingebaut wird. Dabei soll die Null-Hypothese, die besagt, \tdass es keinen messbaren Unterschied gibt, widerlegt werden.  ",
                "Hierbei führt jeder Proband jeden Test in jeglicher Variation durch\n" +
                        "Jeder Proband führt nur eine Aufgabe aus, so können Verfälschungen der Ergebnisse durch Lerneffekte verhindert werden, allerdings sind dafür auch mehr Probanden benötigt. Auch die Probandengruppen müssen gut ausbalanciert sein, damit sie gleich repräsentativ bleiben.",
                "Probanden werden in Gruppen aufgeteilt, diese Gruppen lösen Aufgaben in unterschiedlichen \tReihenfolgen. Dadurch kann der mögliche Lernerfolg einer zuvor gegangen Aufgabe im \tErgebnis neutralisiert werden. Ein Typisches Verfahren hierfür ist das Latin Square.",
                "Kontrollvariablen sind kontrollierbare Einflüsse von Außen, wie z.B. Raum, Geräusche, \tLichtverhältnisse... Werden diese zu stark kontrolliert verliert die Testumgebung an \tRealitätsbezug und Ergebnisse können schlecht auf andere Situationen Übertragen werden. \tDaher kann es sinnvoll sein bestimmte Einflüsse dem Zufall zu überlassen, was allerdings \tschlecht für die Messungen sind, Allerdings sind diese Ergebnisse besser auf andere \tSituationen zu übertragen.",
                "Think Aloud: Hierbei wird der Proband beobachtet und er spricht die ganze Zeit aus, was er macht und was er denkt was passieren soll. Dies gibt eine gute Sicht darauf wie das System genutzt und verstanden wird. Beeinflusst aber möglicherweise die Performance, welche daher nicht mehr zu messen ist.\n" +
                        "Cooperative Evaluation: Auch hier wird der Proband die ganze Zeit beobachtet, allerdings ist noch ein Beobachter dabei und sie können sich während des Tests gegenseitig Fragen stellen. Der Prüfer kann auch Hilfestellungen oder Erklärungen geben. Es gibt dem Probanden die Möglichkeit das System direkt zu beurteilen. Bei diesem Verfahren ist es aber auch nicht möglich Zeit und Fehlerraten zum messen.\n" +
                        "Post-task Walkthrough: Hierbei wird der Proband beobachtet und gefilmt, die Aufnahme wird im direkten Anschluss mit dem Probanden analysiert und er kann Aussagen machen warum er etwas so durchgeführt hat und was er dabei gedacht hat. Der Prüfer hatte währenddessen Zeit sich wichtige Fragen zu überlegen. Solle eingesetzt werden, wenn z.B. Think Aloud nicht geht weil die Aufgabe zu komplex ist. Zeit und Fehler können gemessen werden. ",
                "User Experience Research: Haben das Verständnis eines Nutzers\n" +
                        "Visual Design: Typo, Icons, Animation, Design\n" +
                        "Interaktion Design: Struktur und Verhalten von Apps, Paper MockUps, Use Cases\n" +
                        "Prototyping: Machen komplexe Abläufe greifbar, Anfertigung von funktionalen Prototypen\n" +
                        "Technical Writing: Schreiben Hilfeseiten, achten auf Konsistenz\n" +
                        "The Glue: Interdisziplinäre Aufgaben, Management",
                "zumindest die Principles sollten umgesetzt sein, sie sind besser als überhaupt kein Design. Dazu zählen die Erlernbarkeit, Flexibilität und die Robustheit.",
                "Da sie bereits praktisch eingesetzt wurden und nachweislich einen Nutzen für die Usability \thaben. Sie werden von Nutzern aus anderen Systemen wieder erkannt. Sie verhelfen zu einer \tmaximalen Usability. ",
                "Principles: Erlernbarkeit, Flexibilität und Robustheit\n" +
                        "Goldene Regeln und Heuristiken: Nielson, Shneiderman (Konkreter als Principles)\n" +
                        "Guidelines / Style Guides: Geräte / Technologie spezifisch. IOS humand interface design pattern\n" +
                        "Design Pattern: Wiederverwendbare Strukturen für ein spezifisches Problem. Yahoo! \n" +
                        "Standards: Konkrete detaillierte Designvorgaben. ISO",
                "Learnability: \n" +
                        "Predictability: was nach einer Aktion passiert\n" +
                        "Sysnthesizability: Der Effekt einer vorangegangen Aktion auf den Zustand des Systems verstehen\n" +
                        "Familiarity: Das Übertragen von Dingen aus der realen Welt auf ein System - Affordances\n" +
                        "Generalizability: Übertragung spezifischer Aktionen auf ähnliche Situationen\n" +
                        "Consistancy: IO sollte bei ähnlichen Situationen gleich sein\n" +
                        "Flexibility: \n" +
                        "Dialogue initiative: User oder System können Dialog starten. Filereplacement\n" +
                        "Multithreading: Das System erlaubt dem Nutzer mehr als eine Aufgabe gleichzeitg auszuführen\n" +
                        "Task migration: Das System kann Aufgaben übernehmen. Spellchecker\n" +
                        "Substitutivity: Gleiche Werte austauschen. Cm → inch \n" +
                        "Custimizability: User kann UI anpassen oder das System passt sich an das User Verhalten an\n" +
                        "Robustness: \n" +
                        "Observability: Nutzer erkennt den inneren Zusatand des Systems\n" +
                        "Recoverability: Wurde ein Fehler erkannt kann der Nutzer in korrigieren\n" +
                        "Responsiveness: Schnelles Antworten des Systems\n" +
                        "Task conformances: Der Nutzer kann alle Aufgaben mit Hilfe des Systems erledigen",
                "Sie verhelfen dazu die Usability zu erhöhen und erleichtern die Evaluation des Designs. Sie sind \tbereits bewährt und bieten auch eine Erklärung warum sie funktionieren. Sie dienen auch als \thilfreiche Checkliste beim erstellen des Designs.",
                "Sie enthalten Gestaltungstipps\n" +
                        "Firmen, Organisationen\n" +
                        "Android, iOS, Java",
                "Design Pattern sind eine Lösung für ein spezifisches wiederkehrendes Problem in einem \t\tbestimmten Kontext. Sie wurden durch best practice gesetzt und erklären was gemacht werden \tmuss und aus welchem Grund. Sie sind leicht zu verstehen und dienen damit auch zur \tinterdisziplinären Kommunikation.",
                "Name: leicht erinnerbar\n" +
                        "Problem: im Bezug auf den Nutzen des Systems\n" +
                        "Context: Beschreibung der Situation in der das Problem auftaucht\n" +
                        "Principle: Ein Pattern das auf einem oder mehreren Prinzipien wie user guidences, consistency, errror management aufbaut\n" +
                        "Solution: Eine bewährte Lösung für das Problem\n" +
                        "Why: Warum dies eine Lösung ist\n" +
                        "Example: Erfolgreiche Anwendung, Screenshot oder Erklärung",
                "Konkreter Vorschlag wie ein Problem gelöst werden kann\n" +
                        "Dienen der interdisziplinären Kommunikation\n" +
                        "Erklärt warum dies eine Lösung ist\n" +
                        "Genau zugeschnitten auf Gerät / Technologie\n" +
                        "Nicht standardisiert und recht neu\n" +
                        "Nicht so generisch wie Goldene Regeln und Heuristiken",
                "@TODO",
                "Head: \n" +
                        "Es werden die Position, Lage und Bewegung des Kopfes getrackt. Dazu sind spezielle Sensoren und Kameras benötigt. \n" +
                        "Damit kann der Bildausschnitt eines Bildschirms passend zur Kopfbewegung bewegt werden. \n" +
                        "Dies erzeugt einen natürlichen Blick auf die Szene.\n" +
                        "Diese Technologie ist sehr kostspielig. \n" +
                        "Eye:\n" +
                        "Getrackt wird das Auge durch Reflexionen in den Augen eines Infrarotprojektor.\n" +
                        "Damit können Objekte selektiert oder modifiziert werden. Text kann gescrollt oder Menüpunkte ausgewählt werden. \n" +
                        "Die dazu notwendige Hardware ist bereits sehr günstig zu erwerben.\n" +
                        "Die Lichtverhältnisse müssen gut sein.\n" +
                        "Blink:\n" +
                        "Hierbei wird lediglich das Blinzeln erkannt. \n" +
                        "Damit können Clicks (Selektionen) ausgeführt werden\n" +
                        "Dazu nötig ist lediglich eine Webcam.\n" +
                        "Damit ist es sehr günstig. \n" +
                        "Geringer Umfang an Einsatzmöglichkeiten",
                "Vor dem Nutzer ist ein Infrarotprojektor, dieser befindet sich zu meist nahe neben der \tbenötigten Kamera. Dieser Projektor bestrahlt die Augen des Nutzers, Die Kamera kann nun \tInfrarotreflexionen auf der Hornhaut erkennen. Da für den Menschen Infrarotlicht nicht sichtbar \tist wird er dadurch nicht gestört. Algorithmen berechnen nun in real time den Blickpunkt des \tNutzers.",
                "Blickpunkt und -richtung\n" +
                        "Die Erkennung von Augen an sich\n" +
                        "Augenidentifikation\n" +
                        "Gesten und Pattern der Augen\n" +
                        "Schließen der Augenlider\n" +
                        "Pupillengröße und Erweiterungen\n" +
                        "Augenposition",
                "Würde man den Blickpunkt als Mauszeiger benutzen so würde man ständig unfreiwillig eine \tAktion auswählen. Den Mauszeiger kann man ruhig halten die Augen bewegen sich ständig, \tauch unfreiwillig. Man kann nicht unterscheiden ob eine Person eine Aktion auswählen möchte \toder ob er nur seinen Blick schweifen lässt.",
                "Selection:\n" +
                        "Ein Object wird angeschaut und es vergeht eine Dwelltime oder man drückt eine Taste\n" +
                        "Moving:\n" +
                        "Ein selektiertes Objekt wird mit Hilfe einer Maus und gedrückter Taste an eine neue Position befördert, abgesetzt wird es durch loslassen der Taste. Anstatt den Weg mit der Maus zu beschreiben kann man dies auch mit den Augen.\n" +
                        "Scrolling: \n" +
                        "Am unteren Bildschirmrand befindet sich ein Pfeil/Button, selektiert man diesen so wird der Text automatisch ein Stück weiter gescrollt, dies muss automatisch gesehen denn sobald neuer Content auftaucht würden die Augen automatisch dort hin springen und somit das Scrollen unmöglich machen.\n" +
                        "Menu: \n" +
                        "Ein Pulldown Menü kann durch eine Blick mit einer Dwelltime geöffnet werden, ein Eintrag wird durch eine weitere kurze Dwelltime gehighlighted, ausgwehählt wird dieser durch einen weiteren Blick mit längerer Dwelltime. Geschlossen wird ein Menü durch den Blick daneben und den Ablauf einer Dwelltime. Alternativ kann statt der Dwelltime auch eine Tastendruck verwendet werden.",
                "Ein Computer kann auf viel mehr Arten mit einem Nutzer interagieren. Eye-Gaze interaction soll \teine Erweiterung der Informationseingabe des Nutzers sein. Sie ist sehr schnell und natürlich \tund benötigt wenig Anstrengung. \n" +
                        "\tDiese Geräte sind mobil bzw. wearable und lassen sich damit überall und im Alltag benutzen. \tSie sind allerdings nicht ganz so genau wie stationäre Systeme. ",
                "Damit UI's auch mit ungenaueren Trackern funktionieren müssen Elemente Groß genug \tangelegt sein, damit eine Interaktion möglich ist. ",
                "Hierbei werden Elektroden nah neben dem Auge angebracht und messen die \tSpannungsschwankungen die entstehen wenn sich das Auge bewegt. Dadurch kann eine \tBewegung und die relative Position der Augen erkannt werden. Dies funktioniert auch mit einer \tArt Stecker in der Ohren.",
                "Saccades: \n" +
                        "Da die Hornhaut so beschaffen ist, dass immer nur ein kleiner Ausschnitt des Bildes mit hoher Genauigkeit erkannt werden kann schweift der menschliche Blick über die ganze Szene und erstellt damit ein Gesamtbild. Sie haben eine Dauer von 80ms\n" +
                        "Fixations: \n" +
                        "Dies ist die Zeit zwischen den Sacaddes wo die Augen relativ still stehen. Aber selbst hier gibt es leichte Bewegungen, wenn auch unfreiwillig. \n" +
                        "Vergleich zur Maus:\n" +
                        "Einen Mauszeiger kann man  ruhig an einer Stelle stehen lassen, wohingegen ein Blick sich immer bewegt. Mit einer Maus kann man sehr kleine Objekte mit hoher Genauigkeit auswählen.",
                "Die Augenbewegung kann an die Bewegung von Objekten auf dem Bildschirm angepasst werden. Dadurch ist die Augenbewegung viel glatter und langsamer als bei Saccades and Fixations. Ein Auge verfolgt ein Objekt auf einem Bildschirm.",
                " Z.B. in klassischen Betriebssystemen die zur Bestätigung eines Buttons ein Objekt herumbewegen, das verfolgt werden muss.",
                "Ein Smartphone muss erst aus der Tasche geholt, entsperrt, und in der GUI navigiert werden bis man bei der Gewünschten Applikation/Aktion angekommen ist, die Glass lässt einem dabei die Hände frei und ist sofort einsatzbereit. Die Intention bis zur Aktion verringert sich bei ihr so stark, dass sie schon als ein erweitertes Ich angesehen werden kann. Außerdem können Wearables die Umwelt mit einbeziehen. ",
                "Die meisten Brillen sind recht bullig und fallen dadurch auf\n" +
                        "Sie haben einen schwachen Akku und müssen oft geladen werden\n" +
                        "Das virtuelle Bild ist noch nicht perfekt in das reale integriert, bei der Glass muss man z.B. nach oben schauen um die Informationen zu bekommen\n" +
                        "Interaktionen werden oft über Sprache gestartet, dabei können andere Personen zuhören, was man nicht möchte\n" +
                        "Sicherheitslücken könnten es ermöglichen die aufgenommenen Daten zu stehlen. Diese Daten sind die persönlichsten Daten  ",
                "Sie sollen eine Person im Alltag unterstützen und ihm dabei Aufgaben erleichtern oder die \tErledigung beschleunigen oder Informationen abspeichern. Sie sind eine Erweiterung einer \tSelbst, wie z.B. ein Fahrrad. Sie sollen aber nicht ablenken.",
                "Soll die Aufmerksamkeit des Nutzers auf sich ziehen und ihn an Dinge erinnern. Es werden \tInformationen z.B. über Personen angezeigt. Der User gibt Informationen ein und der \t\tremembrance agent speichert diese und kann sie wiedergeben. Unterstütz den User durch \tSuchanfragen",
                "Entfernte Personen können einem „über die Schulterschauen“ und bei der Erledigung von \tAufgaben beistehen. Diese Technologie kann aber leider auch zur Überwachung missbraucht \twerden.",
                "Augmented Memory:\n" +
                        "Computer können viel mehr Daten abspeichern und effektiver wieder aufrufen. So kann ein Computer unterstützende Informationen für die Erledigung einer Aufgabe oder zur Erinnerung anzeigen.\n" +
                        "Camera Based Reality: \n" +
                        "Physically based Hypertext\n" +
                        "Augmented Reality\n" +
                        "Hilfe für sehbehinderte Nutzer",
                "Haupt Ziel ist es die Zeit zwischen Intention und Aktion zu minimieren, da die Brille bereits \teingeschaltet und betriebsbereit ist und alle Informationen breit stehen schafft sie dies auch im \tGegensatz zum Smartphone, das zunächst aus der Hosentasche geholt werden muss. Dies ist \teine Voraussetzung für ein benutzbares Interface. Wenn es zu lange dauern würde, würde man \tes nicht benutzen. \n" +
                        "\tDa sie Aktionen quasi sofort auslösen kann oder Aufgaben Erledigungen beschleunigt. Wie z.B. \tes ein Fahrrad macht. Der Mensch soll damit schneller und besser arbeiten können. \n" +
                        "\tDie zwei Sekundenregel trifft z.B. bei der Armbanduhr zu, denn ein Blick um die Uhrzeit zu \terfahren dauert 2sec. Da diese Information also jederzeit sofort zur Verfügung steht merken sie \tdie Personen die Uhrzeit nicht oder versuchen sich an sie zu erinnern sondern schauen sie \teinfach noch einmal nach. ",
                "Das Recht am eigenen Bild kann verletzt werden, denn die Brillen nehmen ständig Bilder auf und können diese auch abspeichern. \n" +
                        "Durch Sicherheitslücken kann der Nutzer selber überwacht werden",
                "s.h. Skript Bild Head-mounted displays",
                "Beide Augen sehen unterschiedliche Bilder. Dadurch gerät das Gehirn in einen instabilen Zustand wobei dann zwischen den beiden Augen der Dominate Zustand herrscht. Dies kann zu Schmerzen führen\n" +
                        "Das virtuelle Bild wird für gewöhnlich in einem Abstand von 1-2m dargestellt, die reale Welt kann allerdings einen beliebigen Abstand haben. Dies kann auch zu Schmerzen führen. ",
                "Sehr teuer\n" +
                        "Umgebungsverhältnisse sind nicht optimal, Kameras und Sensoren brauchen z.B. bestimmte Lichtverhältnisse. \n" +
                        "Arbeitsumgebungen verlangen robuste Displays mit langer Akkulaufzeit\n" +
                        "Verfolgen bisher noch keine Standards\n" +
                        "Probleme beim Selektieren der relevanten Informationen\n" +
                        "Tracking oftmals kompliziert",
                "Generell können sie die Arbeit unterstützen durch zusätzliche Informationen, wobei die Hände \tweiterhin frei bleiben. \n" +
                        "Sie können beim Erlernen neuer Fähigkeiten behilflich sein\n" +
                        "Zeigen Bau-/Reperaturanleitungen\n" +
                        "Bauteile können nach Fehlern oder fehlenden Teilen untersucht werden\n",
                "Das ist eine strukturierte, abstrakte taktile Nachricht an den User, welches eine Information \tnonverbal kodiert.",
                "Jedes Tacton steht für ein bestimmtes Object oder eine bestimmte Aktion. Kombiniert man \tdiese kann man so dem User die Information übermitteln, dass z.B. mit Objekt x die Aktion y \tausgeführt wurde.\n" +
                        "\tDie meisten Geräte verfügen nicht über sehr viel arten von Tactons. Meist müsste man dafür \tTöne benutzen, aber wenig Benutzer haben entweder ihren Ton eingeschaltet, da es die \tMitmenschen stören würde oder sie fühlen sich selber belästigt von.",
                "Inertial shaker: \n" +
                        "Ist ein Vibrationsmotor, der das Ganze Gerät in Vibration versetzt\n" +
                        "Dies ist ein sehr starkes Feedback\n" +
                        "Hat eine geringe Auflösung\n" +
                        "linear acurator: \n" +
                        "Ist ein Vibrationsmotor der punktuell eine Vibration hervorbringt. \n" +
                        "Diese Vibration ist sehr stark und kann lokalisiert werden und damit weitere Informationen beinhalten\n" +
                        "Hat ebenfalls eine schlechte Auflösung",
                "Frequenz: \n" +
                        "Duration:\n" +
                        "Waveform:\n" +
                        "Rhythmus:",
                "Da dies dem User ein Gefühl gibt was einem Tastendruck nahe kommt. Zudem ist es ein Feedback dafür, dass eine Taste getroffen wurde. Dadurch kann die Performance gesteigert und die Frustration so wie physische und mentale Ansprüche gesenkt werden. ",
                "Diese könnten z.B. an einem Gürtel an gebracht sein und somit die Richtung anweisen. Oder \tman trägt jeweils einen ums Handgelenk. Damit kann die nächste Richtung angedeutet \twerden. ",
                "Vorteile: Kann direkt in Gerät angebracht sein\n" +
                        "Nachteile: Die Vibrationspattern müssen erkannt und erinnert werden, damit eine Navigation möglich ist. ",
                "Navigation\n" +
                        "Könnte anzeigen welche Art von eingehender Nachtricht es ist (SMS, Email, Anruf)",
                "Vorteile: Ein an sich flacher Bildschirm kann visuelles in ertastbares umwandeln. Damit könnte es Blinden ermöglicht werden Touchscreen Geräte zu verwenden. Es ist zudem sehr schnell und hat eine hohe Auflösung. \n" +
                        "Nachteile: Nur wenn sich der Finger bewegt wird ein Feedback erzeugt. Kann also nicht für Buttonclicks genutzt werden",
                "Durchsichtiges Display durch welches die Umwelt nicht verdeckt wird sondern erweitert. \n" +
                        "Schmutzabweisende Oberfläche die vor Korrosion und Gebrauchsspuren schützt und damit die Lebensdauer verlängert. Auch bleiben keine Fingerabdrücke auf dem Display was auch die Sicherheit erhöht. \n" +
                        "Erweiterte Energiequellen, dazu ist ein Nanogewebe eingebaut, das mit Hilfe des Sonnenlichts den Akku laden kann, der dadurch kleiner ausfällt und das Gerät sich jederzeit laden lässt.\n" +
                        "Die Nanostruktur des Geräts ist flexibel und steckbar, dadurch kann die Displaygröße auf die aktuell gewünschte Größe angepasst/gefaltet werden. Uhr → Phone → Tablet\n" +
                        "Es hat Umgebungssensoren mit denen z.B. die Luftverschmutzung gemessen werden kann und der Nutzer darüber informiert wird. ",
                "Flexible Größe, wodurch die Geräte mobiler werden. Ein Tablet kann so in die Hosentasche gesteckt und überall mit hingenommen werden.\n" +
                        "Das Gerät passt sich dem Körper an und stört nicht in der Tasche\n" +
                        "Es ist robuster, biegen oder ein Sturz machen ihm nichts aus. Smartphones sind ein Alltagsgegenstand und werden daher stark beansprucht",
                "Dies ist ein Gerät das flexibel ist und die Interaktion durch biegen oder durch Berührungen der \tHinterseite erfolgt. \n" +
                        "Vorteile: Es kann mit Handschuhen benutzt werden und man hat keine störenden Fingerabdrücke auf dem Display\n" +
                        "Nachteile: Es kann nicht mit einer Hand interagiert werden. Manche Gesten sind nicht natürlich und verwirren. \n" +
                        "Interactions: Ein Bild kann durch nach oben Biegen heran- und durch nach unten Biegen weggezoomt werden. Dies ist eine klare natürliche Geste. ",
                "Dies ist ein mobiles Gerät das als Bilderbrowser und Musikplayer dient, interagiert wird mit ihm \tähnlich wie mit dem Gummi, durch biegen und drehen des Gerätes. Es ist ein bereits \tfunktionstüchtiger Prototyp. Es kann ebenfalls mit Handschuhen bedient werden und auch \tschon kleine Bewegungen reichen zur Interaktion aus. Nachteile sind, dass viele Gesten \twillkürlich gemappt sind und daher nicht verwirren. Es hat ebenfalls keinen touchscreen und ist \tnur auf der hinteren Seite berührungssensitiv. ",
                "Dies ist ein Telefon aus einem flexiblem e-Ink Displays. Es wird nicht über Touch sonder durch \tVerformung der Ecken und kanten gesteuert. \n" +
                        "Vorteile: \n" +
                        "Es passt sich gut dem Körper an und ist leicht\n" +
                        "Durch das e-Ink Display verbraucht es kaum Akku\n" +
                        "Selbes Look and Fell wie Papier\n" +
                        "Man kann darauf genauso mit einem Stift schreiben wie auf normalem Papier\n" +
                        "Gesten können individuell angepasst werden\n" +
                        "Nachteile: \n" +
                        "Nur schwarz/weiß Display\n" +
                        "Durch die geringe Dicke möglicherweise unpraktisch zum Telefonieren\n" +
                        "Keine Touchbedienung und die Gesten sind evtl. schwer zu merken",
                "Es ist sehr günstig und ist in der Form sehr flexibel und außerdem sehr leicht. Man kann es \tüberall mit hin nehmen oder Verschicken. Durch einscannen oder Abfotografieren ist es auch \tsehr schnell digitalisiert. Es ist ein bewährtes Medium und der Umgang und die Eigenschaften \tsind jedem bekannt. Moderne Geräte mit e-Ink Display bieten zwar zum Lesen den selben \tKomfort sind aber bei weitem nicht so günstig, handlich und flexibel. ",
                "Dies ist ein gerät dessen Display sich ein- und ausziehen lässt, vergleichbar mit einer \tSchriftrolle. Es hat zusätzlich zwei Buttons und einen Trackball. Je nach aktueller Displaygröße \twerden unterschiedliche Aktionen ausgelöst. \n" +
                        "Vorteile: \n" +
                        "Der Content kann auch einfach in der Größe angepasst werden, in dem man beide Buttons gedrückt hält und es entweder auseinander zeiht \toder einfährt. Dies ist ein gutes natürliches Mapping.\n" +
                        "Leicht lässt sich auch von Hoch- in Querformat und umgekehrt wechseln. \n" +
                        "Die Größe kann an den Content angepasst werden und umgekehrt\n" +
                        "Nachteile: \n" +
                        "Keine Touchbedienung\n" +
                        "Noch nicht mobil einsetzbar\n" +
                        "Ungewohnt\n" +
                        "Eine der beiden Dimensionen ist eingeschränkt",
                "Shape-changing devices sind Geräte, die ihre Form selbstständig anpassen. Sie verformen sich \tum dem Nutzer ein Feedback zu geben. Diese Art von Feedback funktioniert in der Umgebung, \tegal ob laut oder belebt. \n" +
                        "Das Morphee ist ein Gerät das seine Form der jeweiligen Aktion anpasst und somit dem User \tdie Interaktion erleichtern möchte. Seine Formen sollen Affordences sein, wodurch der User \tsofort erkennt wie er zu interagieren hat.\n" +
                        "Vorteile: \n" +
                        "Der User erkennt durch die Affordences wie er mit dem Gerät zu agieren hat ohne vorher in dieser Situation gewesen zu sein. Es liefert damit taktiles und habtisches Feedback\n" +
                        "Nachteile: \n" +
                        "Die Auflösung des Displays ist sehr gering\n" +
                        "Eine Form zu halten verbraucht sehr viel Akku",
                "Ein System wird für eine bestimmte Zielgruppe entwickelt, dabei ist eine Persona eine fiktive \trepräsentative Person aus dieser Gruppe mit konkretem Namen, Eigenschaften, Hobbies und \tBeruf.",
                "Sie ist so konkret wie möglich\n" +
                        "sehr realitätsnah\n" +
                        "Sie basiert auf Fakten des User Research\n" +
                        "Einfach zu verstehen und zu unterscheiden ",
                "Ist ein konkreter Anwendungsfall der Software um ein bestimmtes Ziel zu erreichen. Dazu werden Use-Cases mit Nutzer Stories kombiniert un verhelfen zu einem besseren verständnis des Nutzungskontextes. ",
                "Es ist sehr konkret und hat ein funktionales Ziel\n" +
                        "Erzählt eine komplette Geschichte wie sie die Persona erleben würde",
                "Sehr schnell und günstig herzustellen\n" +
                        "Fokus liegt auf der Funktionalität und nicht auf dem Design oder der Implementierung\n" +
                        "Fruhzeitige Evaluation der Grundidee\n" +
                        "Die Hemmschwelle Kritik zu äußern ist noch nicht hoch\n" +
                        "Wünsche der Nutzer können direkt umgesetzt werden",
                "High-fidelity Prototypen sind evtl. bereits funktionierende Teile des späteren Systems, die einen \techten ersten Eindruck für die Interaktion mit dem System zulassen. Sie haben das für das \t\tSystem fertige Look and feel. Sie sind allerdings sehr teuer, da bereits viel Arbeit investiert \twurde. Es ist möglich die Funktionalität zu testen und Systemfehler zu erkennen. Änderungen \tkönnen auch nicht mehr so einfach und schnell vorgenommen werden. ",
                "Ist ein Screen einer App der genau einer Aktion dienen sollte",
                "Ist der Anstoß eine andere Activity von der aktuellen aus zu starten. Diese rückt nun in den \tFokus und kann evtl auch Daten der alten als Parameter übergeben bekommen haben. ",
                "s.h. Lifecycle Zeichnung",
                "MySQLite\n" +
                        "Internal/External/Networkstorage\n" +
                        "Shared Prefernces"
        };
    }
}
