import ilog.concert.*;
import ilog.cplex.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class test {
    public static void solveMe() {
        try {


            int n = 2; //nbre de niveaux
            int[] A = {25, 10, 5}; //cardinal des ensembles Al
            int[] K = {5, 2}; //taille de la gamme dans chaque niveaux(frnsrs)
            int B=10000;//budget
            int[] d = {25, 29, 12, 12, 21, 10, 10, 7, 27, 22, 18, 24, 29, 16, 34, 23, 9, 21, 22, 12, 33, 8, 17, 26, 9};
            int[][][] c = new int[2][10][25]; // J'ai changé la dimension de 1 à 2 pour correspondre à c1.txt et c2.txt
            int[][][] u = new int[2][25][25];
            int[][][] f = new int[2][25][25];


            // Lecture des données à partir du fichier c1.txt
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\All Tech\\Desktop\\c1 (2).txt"));
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 25; j++) {
                        c[0][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }


            // Lecture des données à partir du fichier c2.txt
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\all Tech\\Desktop\\c2.txt"));
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 10; j++) {
                        c[1][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Lecture des données à partir du fichier u1.txt
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\all Tech\\Desktop\\u1.txt"));
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 5; j++) {
                        u[0][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Lecture des données à partir du fichier u2.txt i
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\all Tech\\Desktop\\u2.txt"));
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 2; j++) {
                        u[1][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Lecture des données à partir du fichier f1.txt
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\all Tech\\Desktop\\f1.txt"));
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 5; j++) {
                        f[0][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Lecture des données à partir du fichier f2.txt
            try {
                Scanner scanner = new Scanner(new File("C:\\Users\\all Tech\\Desktop\\f2.txt"));
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 2; j++) {
                        f[1][i][j] = scanner.nextInt();
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            //////Debeug

            System.out.println("Contents of c1.txt:");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 5; j++) {
                    System.out.print(f[0][i][j] + " ");
                }
                System.out.println();  // Move to the next line after printing a row
            }


            //new model
            IloCplex model = new IloCplex();

            //variables

            IloNumVar[][][] X = new IloNumVar[n][][];

            for (int l = 0; l < n; l++) {
                X[l] = new IloNumVar[A[l + 1]][];

                for (int i = 0; i < A[l + 1]; i++) {
                    X[l][i] = new IloNumVar[A[l]];
                    for (int j = 0; j < A[l]; j++) {
                        X[l][i][j] = model.numVar(0, Double.MAX_VALUE);
                    }
                }
            }
//            IloNumVar[][][] V = new IloNumVar[n][][];
//            for (int l = 1; l <= n; l++) {
//                V[l] = new IloIntVar[A[l]][];
//                for (int i = 0; i < A[l]; i++) {
//                    V[l][i] = model.intVarArray(A[l - 1], 0, 1);
//                } //y[j]:array,complete column
//            }
            IloNumVar[][][] Y = new IloNumVar[n][][];

            for (int l = 0; l < n; l++) {
                Y[l] = new IloNumVar[A[l + 1]][];

                for (int i = 0; i < A[l + 1]; i++) {
                    Y[l][i] = new IloNumVar[K[l]];
                    for (int j = 0; j < K[l]; j++) {
                        Y[l][i][j] = model.intVar(0, 1);
                    }
                }
            }


            //Expressions:

            //somme de x[1][i][j]
            IloLinearNumExpr[] quantity_1 = new IloLinearNumExpr[A[0]];
            for (int j = 0; j < A[0]; j++) {
                quantity_1[j] = model.linearNumExpr();
                for (int i = 0; i < A[1]; i++) {
                    quantity_1[j].addTerm(1.0, X[0][i][j]);
                }
                //System.out.println(quantity_1[j]);


            }
            //somme de x[l][i][j]

            IloLinearNumExpr[][] quantity = new IloLinearNumExpr[n][];
            for (int l = 0; l < n; l++) {  // Corrected loop bounds
                quantity[l] = new IloLinearNumExpr[A[l + 1]];
                for (int i = 0; i < A[l + 1]; i++) {
                    quantity[l][i] = model.linearNumExpr();
                    for (int j = 0; j < A[l]; j++) {
                        quantity[l][i].addTerm(1.0, X[l][i][j]);
                    }
                }
            }


            //somme Ul,i,k*Yl,i,k
            IloLinearNumExpr[][] capacity_niv_ouverts = new IloLinearNumExpr[n][];
            for (int l = 0; l < n; l++) {
                capacity_niv_ouverts[l] = new IloLinearNumExpr[A[l + 1]];
                for (int i = 0; i < A[l + 1]; i++) {
                    // Ensure 'k' is within the bounds of your array 'u'
                    capacity_niv_ouverts[l][i] = model.linearNumExpr();
                    for (int k = 0; k < K[l]; k++) {
                        // Ensure 'i' is within the bounds of your array 'Y'
                        capacity_niv_ouverts[l][i].addTerm(u[l][i][k], Y[l][i][k]);
                    }
                }
            }


            //somme Yl,i,k
            IloLinearNumExpr[][] niv_ouverts = new IloLinearNumExpr[n][];
            for (int l = 0; l < n; l++) {
                niv_ouverts[l] = new IloLinearNumExpr[A[l + 1]]; // Initialise niv_ouverts[l]

                for (int i = 0; i < A[l + 1]; i++) {   // i < A[l+1]
                    niv_ouverts[l][i] = model.linearNumExpr();

                    for (int k = 0; k < K[l]; k++) {   // K={5,2}
                        niv_ouverts[l][i].addTerm(1.0, Y[l][i][k]);
                    }
                }
            }


//            //Vlij
//            IloLinearNumExpr[][][] verif_fourniture = new IloLinearNumExpr[n][n][n + 1];
//            for (int l = 1; l < n + 1; l++) {
//                for (int i = 0; i < A[l]; i++) {//i<n
//                    for (int j = 0; j < A[l - 1]; j++) {
//                        verif_fourniture[l][i][j] = model.linearNumExpr();
//                    }
//                }
//            }
            //contrainte budgetaire: somme Yl,i,k*fl,i,k
            IloLinearNumExpr budget_ouvert = model.linearNumExpr();
            for (int l = 0; l < n; l++) {
                for (int i = 0; i < A[l + 1]; i++) {
                    for (int k = 0; k < K[l]; k++) {
                        budget_ouvert.addTerm(f[l][i][k], Y[l][i][k]);
                    }
                }
            }

            //somme xl,j,p: quantite recue
            IloLinearNumExpr[][] quantity_al_1 = new IloLinearNumExpr[n - 1][];
            for (int l = 1; l < n; l++) {
                quantity_al_1[l - 1] = new IloLinearNumExpr[A[l]];
                for (int j = 0; j < A[l]; j++) {
                    quantity_al_1[l - 1][j] = model.linearNumExpr();
                    for (int p = 0; p < A[l - 1]; p++) {
                        quantity_al_1[l - 1][j].addTerm(1.0, X[l - 1][j][p]);
                    }
                }
            }

            //somme xl,i,j de la derniere contrainte: quantite envoyee
            IloLinearNumExpr[][] quantity_al = new IloLinearNumExpr[n - 1][];
            for (int l = 1; l < n; l++) {
                quantity_al[l - 1] = new IloLinearNumExpr[A[l]];
                for (int j = 0; j < A[l]; j++) {
                    quantity_al[l - 1][j] = model.linearNumExpr();
                    for (int i = 0; i < A[l + 1]; i++) quantity_al[l - 1][j].addTerm(1.0, X[l][i][j]);
                }
            }

            //expression Objective
            IloLinearNumExpr objective = null;
            try {
                objective = model.linearNumExpr();
            } catch (IloException ex) {
                throw new RuntimeException(ex);
            }
            for (int l = 0; l < n; l++) {
                for (int i = 0; i < A[l + 1]; i++) {
                    for (int j = 0; j < A[l]; j++) {
                        objective.addTerm(c[l][i][j], X[l][i][j]);
                    }
                }
            }

            //objective
            try {
                model.addMinimize(objective);
            } catch (IloException e) {
                throw new RuntimeException(e);
            }
            //Constraintes

            //1ere contrainte: somme xa,i,j=dj

            for (int j = 0; j < A[0]; j++) {
                model.addEq(quantity_1[j], d[j]);
            }
            //2eme Contrainte
            for (int l = 1; l < n + 1; l++) {  //l<n+1
                for (int i = 0; i < A[l]; i++) {
                    model.addLe(quantity[l-1][i], capacity_niv_ouverts[l-1][i]);
                }
            }
//            //3eme Contrainte
//            for (int l = 1; l < n + 1; l++) {
//                for (int i = 0; i < A[l]; i++) {
//                    for (int j = 0; j < A[l - 1]; j++) model.addLe(verif_fourniture[l][i][j], niv_ouverts[l][i]);
//                }
//            }
            //4eme Contrainte
            for (int l = 1; l < n + 1; l++) {
                for (int i = 0; i < A[l]; i++) {
                    model.addLe(niv_ouverts[l-1][i], 1.0);
                }
            }
            //5eme Contrainte
//            for (int l = 1; l < n + 1; l++) {
//                for (int i = 0; i < A[l]; i++) {
//                    for (int k = 0; k < K[l - 1]; k++) {
            model.addLe(budget_ouvert, B);

            //6eme Contrainte
            for (int l = 2; l < n + 1; l++) {
                for (int j = 0; j < A[l - 1]; j++)
                    model.addLe(quantity_al_1[l-2][j],quantity_al[l-2][j]);
            }

            // solve model
            if (model.solve()) {
                System.out.println("obj = " + model.getObjValue());
            } else {
                System.out.println("problem not solved");
            }
            ////decision vriables:
            for (int l = 0; l < n; l++) {
                for (int i = 0; i < A[l + 1]; i++) {
                    for (int j = 0; j < A[l]; j++) {
                        try {
                            System.out.println("X[" + l + "][" + i + "][" + j + "] = " + model.getValue(X[l][i][j]));
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            ////////les Y
            for (int l = 0; l < n; l++) {
                for (int i = 0; i < A[l + 1]; i++) {
                    for (int j = 0; j < K[l]; j++) {
                        try {
                            System.out.println("Y[" + l + "][" + i + "][" + j + "] = " + model.getValue(Y[l][i][j]));
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            model.end();

        } catch (IloException e) {
            throw new RuntimeException(e);
        } finally {

        }
    }

}







;