using System;
using System.IO;

namespace TinyGP
{
    internal class TinyGP
    {
        public static int Main(string[] args)
        {
            var fname = "problem.dat";
            long s = -1;

            if (args.Length >= 2)
            {
                s = int.Parse(args[0]);
                fname = args[1];
            }

            if (args.Length == 1) fname = args[0];

            var gp = new tiny_gp(fname, s);
            return gp.evolve();
        }
    }

    internal class tiny_gp
    {
        private const byte 
            ADD = 110,
            SUB = 111,
            MUL = 112,
            DIV = 113,
            FSET_START = ADD,
            FSET_END = DIV;

        private const int 
            MAX_LEN = 10000,
            POPSIZE = 100000,
            DEPTH = 5,
            GENERATIONS = 100,
            TSIZE = 2;

        private const double 
            PMUT_PER_NODE = 0.05,
            CROSSOVER_PROB = 0.9;

        private static readonly double[] x = new double[FSET_START];
        private static double minrandom, maxrandom;
        private static byte[] program;
        private static int PC;
        private static int varnumber, fitnesscases, randomnumber;
        private static double fbestpop, favgpop;
        private static long seed;
        private static double avg_len;
        private static Random rd = new Random();
        private static double[][] targets;

        private static readonly byte[] buffer = new byte[MAX_LEN];

        private readonly char[] separator = {' ', '\t', '\n', '\r', '\f'};

        private readonly double[] fitness;
        private readonly byte[][] pop;

        public tiny_gp(string fname, long s)
        {
            fitness = new double[POPSIZE];
            seed = s;
            if (seed >= 0)
                rd = new Random((int) seed);
            setup_fitness(fname);
            for (var i = varnumber; i < FSET_START; i++)
                x[i] = (maxrandom - minrandom) * rd.NextDouble() + minrandom;
            pop = create_random_pop(POPSIZE, DEPTH, fitness);
        }

        ///read parameters and input/output examples from input file
        private void setup_fitness(string fname)
        {
            var isHeader = true;
            var targetIndex = 0;
            foreach (var line in File.ReadLines(fname))
            {
                var tokenList = line.Split(separator, StringSplitOptions.RemoveEmptyEntries);
                if (isHeader)
                {
                    isHeader = false;
                    var tokenIndex = 0;
                    varnumber = int.Parse(tokenList[tokenIndex++]);
                    randomnumber = int.Parse(tokenList[tokenIndex++]);
                    minrandom = double.Parse(tokenList[tokenIndex++]);
                    maxrandom = double.Parse(tokenList[tokenIndex++]);
                    fitnesscases = int.Parse(tokenList[tokenIndex]);
                    targets = new double[fitnesscases][];
                    if (varnumber + randomnumber >= FSET_START)
                        Console.WriteLine("too many variables and constants");
                }
                else
                {
                    targets[targetIndex] = new double[varnumber + 1];
                    for (var j = 0; j <= varnumber; j++) targets[targetIndex][j] = double.Parse(tokenList[j]);
                    targetIndex++;
                }
            }
        }

        private byte[][] create_random_pop(int popsize, int depth, double[] pFitness)
        {
            var pop = new byte[popsize][];
            int i;

            for (i = 0; i < popsize; i++)
            {
                pop[i] = create_random_indiv(depth);
                pFitness[i] = fitness_function(pop[i]);
            }

            return pop;
        }

        private byte[] create_random_indiv(int depth)
        {
            var len = grow(buffer, 0, MAX_LEN, depth);

            while (len < 0)//since 0 always < MAX_LEN, len<0 means buffer represents an incorrect tree
                len = grow(buffer, 0, MAX_LEN, depth);

            var ind = new byte[len];

            Buffer.BlockCopy(buffer, 0, ind, 0, len);
            return ind;
        }

        ///return next position to grow (equal to the length occupied), or -1 to signal the end of grow because of exceeding max len or incorrect number of operand
        private int grow(byte[] pBuffer, int position, int maxLen, int depth)
        {
            while (true)
            {
                if (position >= maxLen) return -1;

                var prim = position == 0 ? (byte) 1 : (byte) rd.Next(2); //50% operator,50% random constant or variable (input parameters), and force the root node must be an operator

                if (prim == 0 || depth == 0 || position + 2 >= maxLen) //if depth is exceeded or there is no room for an operand
                {
                    prim = (byte) rd.Next(varnumber + randomnumber); //refer to one of input parameters or random constants
                    pBuffer[position] = prim;
                    return position + 1;
                }

                prim = (byte) (rd.Next(FSET_END - FSET_START + 1) + FSET_START);
                switch (prim)
                {
                    case ADD:
                    case SUB:
                    case MUL:
                    case DIV:
                        pBuffer[position] = prim;
                        var firstChild = grow(pBuffer, position + 1, maxLen, depth - 1);
                        if (firstChild < 0) return -1; //reach max length
                        position = firstChild;
                        depth = depth - 1;
                        continue;
                    default:
                        throw new Exception();
                    //return 0; // should never get here
                }
            }
        }

        //prog is linear formated program source code in prefix tree order
        private double fitness_function(byte[] prog)
        {
            //var len = traverse(prog, 0);
            var fit = 0.0;
            for (var i = 0; i < fitnesscases; i++)
            {
                for (var j = 0; j < varnumber; j++) //fill in one input datas
                    x[j] = targets[i][j];
                program = prog;
                PC = 0;
                var result = Run();
                fit += Math.Abs(result - targets[i][varnumber]);
            }

            return -fit;
        }

        //return next position to traverse
        private int Traverse(byte[] programTree, int position)
        {
            while (true)
            {
                if (programTree[position] < FSET_START) return ++position;

                switch (programTree[position])
                {
                    case ADD:
                    case SUB:
                    case MUL:
                    case DIV:
                        position = Traverse(programTree, ++position);
                        continue;
                    default:
                        throw new Exception();
                    //return( -1 ); // should never get here
                }
            }
        }

        private double Run()
        {
            var primitive = program[PC++];
            if (primitive < FSET_START)
                return x[primitive];
            switch (primitive)
            {
                case ADD: return Run() + Run();
                case SUB: return Run() - Run();
                case MUL: return Run() * Run();
                case DIV:
                {
                    double num = Run(), den = Run();
                    if (Math.Abs(den) <= 0.001) // guarded division
                        return num;
                    return num / den;
                }
                default:
                    throw new Exception();
            }
            //return 0.0; // should never get here
        }

        public int evolve()
        {
            int gen = 0, indivs, offspring, parent1, parent2, parent;
            double newfit;
            byte[] newind;
            print_parms();
            stats(fitness, pop, 0);
            for (gen = 1; gen < GENERATIONS; gen++)
            {
                if (fbestpop > -1e-5)
                {
                    Console.WriteLine("PROBLEM SOLVED\n");
                    return 0;
                }

                for (indivs = 0; indivs < POPSIZE; indivs++)
                {
                    if (rd.NextDouble() < CROSSOVER_PROB)
                    {
                        parent1 = tournament(fitness, TSIZE);
                        parent2 = tournament(fitness, TSIZE);
                        newind = crossover(pop[parent1], pop[parent2]);
                    }
                    else
                    {
                        parent = tournament(fitness, TSIZE);
                        newind = mutation(pop[parent], PMUT_PER_NODE);
                    }

                    newfit = fitness_function(newind);
                    offspring = negative_tournament(fitness, TSIZE);
                    pop[offspring] = newind;
                    fitness[offspring] = newfit;
                }

                stats(fitness, pop, gen);
            }

            Console.WriteLine("PROBLEM *NOT* SOLVED\n");
            return 1;
        }

        private int tournament(double[] fitness, int tsize)
        {
            int best = rd.Next(POPSIZE), i, competitor;
            var fbest = -1.0e34;

            for (i = 0; i < tsize; i++)
            {
                competitor = rd.Next(POPSIZE);
                if (fitness[competitor] > fbest)
                {
                    fbest = fitness[competitor];
                    best = competitor;
                }
            }

            return best;
        }

        private int negative_tournament(double[] fitness, int tsize)
        {
            int worst = rd.Next(POPSIZE), i, competitor;
            var fworst = 1e34;

            for (i = 0; i < tsize; i++)
            {
                competitor = rd.Next(POPSIZE);
                if (fitness[competitor] < fworst)
                {
                    fworst = fitness[competitor];
                    worst = competitor;
                }
            }

            return worst;
        }

        private byte[] crossover(byte[] parent1, byte[] parent2)
        {
            int xo1start, xo1end, xo2start, xo2end;
            byte[] offspring;
            var len1 = Traverse(parent1, 0);
            var len2 = Traverse(parent2, 0);
            int lenoff;

            xo1start = rd.Next(len1);
            xo1end = Traverse(parent1, xo1start);

            xo2start = rd.Next(len2);
            xo2end = Traverse(parent2, xo2start);

            lenoff = xo1start + (xo2end - xo2start) + (len1 - xo1end);

            offspring = new byte[lenoff];

            Buffer.BlockCopy(parent1, 0, offspring, 0, xo1start);
            Buffer.BlockCopy(parent2, xo2start, offspring, xo1start,
                xo2end - xo2start);
            Buffer.BlockCopy(parent1, xo1end, offspring,
                xo1start + (xo2end - xo2start),
                len1 - xo1end);

            return offspring;
        }

        private byte[] mutation(byte[] parent, double pmut)
        {
            int len = Traverse(parent, 0), i;
            int mutsite;
            var parentcopy = new byte [len];

            Buffer.BlockCopy(parent, 0, parentcopy, 0, len);
            for (i = 0; i < len; i++)
                if (rd.NextDouble() < pmut)
                {
                    mutsite = i;
                    if (parentcopy[mutsite] < FSET_START)
                        parentcopy[mutsite] = (byte) rd.Next(varnumber + randomnumber);
                    else
                        switch (parentcopy[mutsite])
                        {
                            case ADD:
                            case SUB:
                            case MUL:
                            case DIV:
                                parentcopy[mutsite] =
                                    (byte) (rd.Next(FSET_END - FSET_START + 1)
                                            + FSET_START);
                                break;
                        }
                }

            return parentcopy;
        }

        private void stats(double[] pFitness, byte[][] pPop, int gen)
        {
            int i, best = rd.Next(POPSIZE);
            var node_count = 0;
            fbestpop = pFitness[best];
            favgpop = 0.0;

            for (i = 0; i < POPSIZE; i++)
            {
                node_count += Traverse(pPop[i], 0);
                favgpop += pFitness[i];
                if (pFitness[i] > fbestpop)
                {
                    best = i;
                    fbestpop = pFitness[i];
                }
            }

            avg_len = (double) node_count / POPSIZE;
            favgpop /= POPSIZE;
            Console.Write("Generation=" + gen + " Avg Fitness=" + -favgpop +
                          " Best Fitness=" + -fbestpop + " Avg Size=" + avg_len +
                          "\nBest Individual: ");
            print_indiv(pPop[best], 0);
            Console.Write("\n");
        }

        private int print_indiv(byte[] buffer, int buffercounter)
        {
            int a1 = 0, a2;
            if (buffer[buffercounter] < FSET_START)
            {
                if (buffer[buffercounter] < varnumber)
                    Console.Write("X" + (buffer[buffercounter] + 1) + " ");
                else
                    Console.Write(x[buffer[buffercounter]]);
                return ++buffercounter;
            }

            switch (buffer[buffercounter])
            {
                case ADD:
                    Console.Write("(");
                    a1 = print_indiv(buffer, ++buffercounter);
                    Console.Write(" + ");
                    break;
                case SUB:
                    Console.Write("(");
                    a1 = print_indiv(buffer, ++buffercounter);
                    Console.Write(" - ");
                    break;
                case MUL:
                    Console.Write("(");
                    a1 = print_indiv(buffer, ++buffercounter);
                    Console.Write(" * ");
                    break;
                case DIV:
                    Console.Write("(");
                    a1 = print_indiv(buffer, ++buffercounter);
                    Console.Write(" / ");
                    break;
            }

            a2 = print_indiv(buffer, a1);
            Console.Write(")");
            return a2;
        }

        private void print_parms()
        {
            Console.WriteLine("-- TINY GP (Java version) --\n");
            Console.WriteLine("SEED=" + seed + "\nMAX_LEN=" + MAX_LEN +
                              "\nPOPSIZE=" + POPSIZE + "\nDEPTH=" + DEPTH +
                              "\nCROSSOVER_PROB=" + CROSSOVER_PROB +
                              "\nPMUT_PER_NODE=" + PMUT_PER_NODE +
                              "\nMIN_RANDOM=" + minrandom +
                              "\nMAX_RANDOM=" + maxrandom +
                              "\nGENERATIONS=" + GENERATIONS +
                              "\nTSIZE=" + TSIZE +
                              "\n----------------------------------\n");
        }
    }
}