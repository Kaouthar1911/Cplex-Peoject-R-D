import numpy as np  # Correction de la faute de frappe

# Fonction pour générer le vecteur aléatoire d_values
def generate_d(A_0):
    return np.random.uniform(5, 36, size=A_0)

# Fonction pour générer la liste de matrices aléatoires c_values
def generate_c(L, A):
    c_values = []
    for l in range(L):
        c_values.append(np.random.uniform(20, 201, size=(A[l + 1], A[l])))
    return c_values

# Fonction pour générer la liste de matrices aléatoires u_values
def generate_u(L, A, K):
    u_values = []
    for l in range(L):
        u_values.append(np.random.uniform(10 + 10 * l, 160 + 540 * l, size=(A[l + 1], K[l])))
    return u_values

# Fonction pour générer la liste de matrices aléatoires f_values
def generate_f(L, A, K, u_values):
    f_values = []
    for l in range(L):
        f_values.append(np.random.uniform(0, 90, size=(A[l + 1], K[l])) + np.random.uniform(100, 110, size=(A[l + 1], K[l])) * np.sqrt(u_values[l]))
    return f_values

# Prendre les entrées de l'utilisateur pour L, A, et K
L = int(input("Enter the value of L: "))
A = [int(input(f"Enter the value of A[{i}]: ")) for i in range(L + 1)]
K = [int(input(f"Enter the value of K[{i}]: ")) for i in range(L)]

# Générer les données en utilisant les fonctions définies
d_values = generate_d(A[0])
c_values = generate_c(L, A)
u_values = generate_u(L, A, K)
f_values = generate_f(L, A, K, u_values)

# Sauvegarder les valeurs générées dans des fichiers texte
np.savetxt('d_values.txt', d_values, fmt='%f')
for l in range(L):
    np.savetxt(f'c_values_{l}.txt', c_values[l], fmt='%f')
    np.savetxt(f'u_values_{l}.txt', u_values[l], fmt='%f')
    np.savetxt(f'f_values_{l}.txt', f_values[l], fmt='%f')

# Sauvegarder les paramètres dans un fichier texte
np.savetxt('parameters.txt', [L] + A + K, fmt='%d')
