import ilog.concert.*;
import ilog.cplex.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Modele_math {
    public static void main(String[] args) {
        try {
            IloCplex cplex = new IloCplex();
            int B=5000;//Budget
            int L;// Nombre de niveaux
            int[] A; // table dont on va stocker les taills de chaque site.

            int[] K;//table dont on va stocker les taills des niveaux de capacité de chaque niveau d'affectation


            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\All Tech\\Documents\\compta\\parameters.txt"));
                //le fichier parametres contient les differentes cardinaux d'emsembles L ,Al,Kl entrés comme inputs pour le générateur.

                L = scanner.nextInt();//Lire Le nombre de niveaux d'affectation a partire de fichiers parametres
                A = new int[L + 1];
                K = new int[L];
                for (int i = 0; i <= L; i++) {
                    A[i] = scanner.nextInt();//lire les nombres d'installations dans chaque site Al.
                }
                for (int i = 0; i < L; i++) {
                    K[i] = scanner.nextInt();//Lire le nombre des niveaux de capacité Kl
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }


            //X[i][j][l] quantité de produits transféré depuis l'installation i vers le client j dans le niveau d'affectation l
            IloNumVar[][][] X = new IloNumVar[L][][];
            for (int l = 0; l < L; l++) {
                X[l] = new IloNumVar[A[l + 1]][];
                for (int i = 0; i < A[l + 1]; i++) {
                    X[l][i] = new  IloNumVar[A[l]];
                    for (int j=0;j<A[l];j++){
                        X[l][i][j]=cplex.numVar(0,Double.MAX_VALUE);
                    }
                }
            }

            //Y[i][k][l] = 1 si l'installation i est ouvert et utilisée à un niveau de capacité k dans le niveau d'affectation l
            IloNumVar[][][] Y = new IloNumVar[L][][];
            for (int l = 0; l < L; l++) {
                Y[l] = new IloNumVar[A[l + 1]][];
                for (int i = 0; i < A[l + 1]; i++) {
                    Y[l][i] = new IloNumVar[K[l]];
                    for(int k=0;k<K[l];k++){
                        Y[l][i][k]= cplex.boolVar();
                    }
                }
            }



            //C[i][j][l] : Coût unitaire d'affectation de l'installation i au client j au niveau d'affectation l.


            Double[][][] C = new Double[L][][];
            for (int l = 0; l < L; l++) {
                C[l] = new Double[A[l + 1]][A[l]];

                try {
                    String fileName = String.format( "C:\\Users\\All Tech\\Documents\\compta\\c_values_%d.txt",l) ;
                    //les fichier c_values_%d.txt contient les coûts d'affectation générés par le générateur , %d varie en fonction de l.
                    Scanner scanner = new Scanner(new File(fileName));

                    for (int i = 0; i < A[l + 1]; i++) {
                        for (int j = 0; j < A[l]; j++) {
                            if (scanner.hasNextDouble()) {
                                C[l][i][j] = scanner.nextDouble(); //lire les différentes coûts d'affectation .
                            }
                        }
                    }

                    scanner.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            //u[l][k][i] : niveau de capacité k au niveau de d'affectation l de l'installation i


            Double[][][] u = new Double[L][][];
            for (int l = 0; l < L; l++) {
                u[l] = new Double[A[l + 1]][K[l]];

                try {
                    String fileName = String.format( "C:\\Users\\All Tech\\Documents\\compta\\u_values_%d.txt",l) ;
                    //les fichier u_values_%d.txt contient les niveaux de capacités générés par le générateur , %d varie en fonction de l.
                    Scanner scanner = new Scanner(new File(fileName));

                    for (int i = 0; i < A[l + 1]; i++) {
                        for (int k = 0; k < K[l]; k++) {
                            if (scanner.hasNextDouble()) {
                                u[l][i][k] = scanner.nextDouble();//lire les différentes niveaux de capacités  .
                            }
                        }
                    }

                    scanner.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

//f[l][i][k] : coût d'oeuverture de l'installation i avec une capacité k au niveau d'affectation l
            Double[][][] f = new Double[L][][];
            for (int l = 0; l < L; l++) {
                f[l] = new Double[A[l + 1]][K[l]];

                try {
                    String fileName = String.format( "C:\\Users\\All Tech\\Documents\\compta\\f_values_%d.txt",l) ;
                    //les fichier f_values_%d.txt contient les coûts d'ouvertures générés par le générateur , %d varie en fonction de l.
                    Scanner scanner = new Scanner(new File(fileName));

                    for (int i = 0; i < A[l + 1]; i++) {
                        for (int k = 0; k < K[l]; k++) {
                            if (scanner.hasNextDouble()) {
                                f[l][i][k] = scanner.nextDouble();//Lire les différentes coûts d'ouvertures
                            }
                        }
                    }

                    scanner.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }


            //d[j]: la demande du client j.
            Double[] d = new Double[A[0]];

            try {
                String fileName=String.format( "C:\\Users\\All Tech\\Documents\\compta\\d_values.txt") ;
                //le fichier d_values.txt contient la demande des clients finnaux.
                Scanner scanner = new Scanner(new File(fileName));

                for (int j = 0; j < A[0]; j++) {
                    if (scanner.hasNextDouble()) {
                        d[j] = scanner.nextDouble();//Lire  la demande de chaque client final j.
                    }
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            //quantité transférée à chaque client j .
            IloLinearNumExpr[]  Quantite_fournie= new IloLinearNumExpr[A[0]];

            for (int j=0; j<A[0]; j++) {
                Quantite_fournie[j] = cplex.linearNumExpr();


                for (int i=0; i<A[1]; i++) {
                    Quantite_fournie[j].addTerm(1.0, X[0][i][j]);
                }
            }
            //Niveau de capacité choisie par chaque installation i.
            IloLinearNumExpr[][] Capacite_choisie=new IloLinearNumExpr[L][];
            for(int l=0;l<L;l++){
                Capacite_choisie[l]=new IloLinearNumExpr[A[l+1]];
                for(int i=0;i<A[l+1];i++){
                    Capacite_choisie[l][i]=cplex.linearNumExpr();
                    for(int k=0;k<K[l];k++){
                        Capacite_choisie[l][i].addTerm(u[l][i][k],Y[l][i][k]);
                    }
                }
            }

            //Capacite consomée (ou bien la quantité fournie ) par chaque installation i
            IloLinearNumExpr[][] Capacite_cosomee=new IloLinearNumExpr[L][];
            for(int l=0;l<L;l++){
                Capacite_cosomee[l]=new IloLinearNumExpr[A[l+1]];
                for (int i=0;i<A[l+1];i++){
                    Capacite_cosomee[l][i]=cplex.linearNumExpr();
                    for(int j=0;j<A[l];j++){
                        Capacite_cosomee[l][i].addTerm(1,X[l][i][j]);
                    }
                }
            }
//Nombre de niveaux de capacite choisie(cette expression doit etre = 1 ,
// pour garantire que chaque installation a choisi un et un seul niveau de capacité)
            IloLinearNumExpr[][] Nmbre_Capacite_choisie = new IloLinearNumExpr[L][];
            for(int l = 0; l < L; l++){
                Nmbre_Capacite_choisie[l] = new IloLinearNumExpr[A[l+1]];
                for(int i = 0; i < A[l+1]; i++){
                    Nmbre_Capacite_choisie[l][i] = cplex.linearNumExpr();
                    for(int k = 0; k < K[l]; k++){
                        Nmbre_Capacite_choisie[l][i].addTerm(1, Y[l][i][k]);
                    }
                }
            }

            //Budget consomee (combient on a consommer du budget dont on dispose)

            IloLinearNumExpr Budget_consomee = cplex.linearNumExpr();
            for(int l=0;l<L;l++){
                for (int i=0;i<A[l+1];i++){
                    for (int k=0;k<K[l];k++){
                        Budget_consomee.addTerm(f[l][i][k],Y[l][i][k]);
                    }
                }
            }

            //flux sortie du niveau l.(le flux de produits recue par l'installatio j  depuis les fournisseurs i dans le niveau l)
            IloLinearNumExpr[][]  Flux_sortie=new IloLinearNumExpr[L-1][];
            for (int  l=1;l<L;l++){
                Flux_sortie[l-1]=new IloLinearNumExpr[A[l]];
                for (int j=0;j<A[l];j++) {
                    Flux_sortie[l-1][j]=cplex.linearNumExpr();
                    for (int i=0;i<A[l+1];i++) {
                        Flux_sortie[l-1][j].addTerm(1,X[l][i][j]);
                    }
                }
            }

            //flux recue au niveau l-1.(le flux de produits sortie par l'installation j aux client p dans le niveau l-1)

            IloLinearNumExpr[][]  Flux_recu=new IloLinearNumExpr[L-1][];
            for (int  l=1;l<L;l++){
                Flux_recu[l-1]=new IloLinearNumExpr[A[l]];
                for (int j=0;j<A[l];j++) {
                    Flux_recu[l-1][j]=cplex.linearNumExpr();
                    for (int p=0;p<A[l-1];p++) {
                        Flux_recu[l-1][j].addTerm(1,X[l-1][j][p]);
                    }
                }
            }

            //Coût total à minimiser.
            IloLinearNumExpr Total_cost= cplex.linearNumExpr();
            for (int l=0;l<L;l++){
                for(int i=0;i<A[l+1];i++){
                    for(int j=0;j<A[l];j++){
                        Total_cost.addTerm(C[l][i][j],X[l][i][j]);
                    }
                }
            }
            // Fonction objectif
            cplex.addMinimize(Total_cost);

            //contrainte de la demande
            for(int j=0;j<A[0];j++){
                cplex.addEq(Quantite_fournie[j],d[j]);

            }

            //Contrainte de capacite.
            for(int l=0;l<L;l++){
                for(int i=0;i<A[l+1];i++){
                    cplex.addLe(Capacite_cosomee[l][i],Capacite_choisie[l][i]);
                }
            }

            //chaque installation doit choisire un et un seul niveau de capacité.
            for(int l=0;l<L;l++){
                for(int i=0;i<A[l+1];i++){
                    cplex.addLe(Nmbre_Capacite_choisie[l][i],1);
                }
            }

            //contrainte budgétaire.
            cplex.addLe(Budget_consomee,B);

            //contraint de flux , le flux de produit recue par l'installation j doit etre
            //supérieur au egale au flux de produit fornie par cette installation au niveau inferieur
            for(int  l=1;l<L;l++){
                for(int j=0;j<A[l];j++){
                    cplex.addGe(Flux_sortie[l-1][j],Flux_recu[l-1][j]);
                }
            }

            // Solve the model
            if (cplex.solve()) {
                // Retrieve and interpret results
                System.out.println("Objective Value = " + cplex.getObjValue());
                ////decision vriables:
                System.out.println();
                System.out.println("voila le variable de décision X[l][i][j]");
                System.out.println();
                for (int l = 0; l < L; l++) {
                    for (int i = 0; i < A[l + 1]; i++) {
                        for (int j = 0; j < A[l]; j++) {
                            try {
                                System.out.println("X[" + l + "][" + i + "][" + j + "] = " + cplex.getValue(X[l][i][j]));
                            } catch (IloException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                System.out.println();
                System.out.println("voila le variable de décision Y[i][l][k]");
                System.out.println();
                for (int l = 0; l < L; l++) {
                    for (int i = 0; i < A[l + 1]; i++) {
                        for (int k = 0; k < K[l]; k++) {
                            try {
                                System.out.println("Y[" + l + "][" + i + "][" + k + "] = " + cplex.getValue(Y[l][i][k]));
                            } catch (IloException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

            }
            else {
                System.out.println("problem not solved");
            }


            cplex.end();
        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }
}