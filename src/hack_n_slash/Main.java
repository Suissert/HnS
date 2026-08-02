package hack_n_slash;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import hack_n_slash.bots.*;
import hack_n_slash.engines.*;
import hack_n_slash.map.MatrixLogic;
import hack_n_slash.miscellaneous.*;

public class Main {
	 
	public static void main(String[] args) {
		Reflections reflections = new Reflections("hack_n_slash.bots"); // package where Bot subclasses are
        Set<Class<? extends Bot>> botClasses = reflections.getSubTypesOf(Bot.class);

        Class<? extends Bot>[] botClassesV = botClasses.toArray((Class<? extends Bot>[]) new Class<?>[0]);
        System.out.println("Available Bots:");
        for (int i=0; i<botClassesV.length; i++) {
        	System.out.println(i + ": " + botClassesV[i].getSimpleName());
        }
        
        // SELEZIONE BOT
        Bot[] botArray = new Bot[2];
        try {
			botArray[0] = botClassesV[0].getDeclaredConstructor().newInstance();
			botArray[1] = botClassesV[1].getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
        
        //INIZIALIZZAZIONE
        MatrixLogic ml = new MatrixLogic(9, 15);
        ml.generaMatrice();
        
        //TURNI
        Engine ngin = new Engine1stEdition(ml.getMatrix(), botArray[0], botArray[1]);
        ngin.start();
        
        
        
	}
	
}
