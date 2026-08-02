package hack_n_slash;

import java.util.Set;

import org.reflections.Reflections;

import hack_n_slash.bots.*;
import hack_n_slash.engines.*;
import hack_n_slash.graphics.*;
import hack_n_slash.map.MatrixLogic;

public class Main {

    public static void main(String[] args) {
        Reflections reflections = new Reflections("hack_n_slash.bots");
        Set<Class<? extends Bot>> botClasses = reflections.getSubTypesOf(Bot.class);

        Class<? extends Bot>[] botClassesV = botClasses.toArray((Class<? extends Bot>[]) new Class<?>[0]);
        System.out.println("Available Bots:");
        for (int i = 0; i < botClassesV.length; i++) {
            System.out.println(i + ": " + botClassesV[i].getSimpleName());
        }

        Bot[] botArray = new Bot[2];
        try {
            botArray[0] = botClassesV[0].getDeclaredConstructor().newInstance();
            botArray[1] = botClassesV[1].getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        GameView view = new WebView();

        MatrixLogic ml = new MatrixLogic(9, 15);
        ml.generaMatrice();

        Engine ngin = new Engine1stEdition(ml.getMatrix(), botArray[0], botArray[1], view);
        ngin.start();

        // Mantieni il server alive così il browser mostra il vincitore
        System.out.println("Partita terminata. Chiudi con Ctrl+C.");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        }
    }
}