package com.travelsync.analysis;

import java.util.Arrays;
import java.util.Random;
import java.util.function.LongBinaryOperator;

/** Hand-built algorithm demonstrations for roadmap modules 5 and 6. */
public final class AdvancedAlgorithms {
    private AdvancedAlgorithms() { }

    public record Clause(int a, int b, int c) { }
    public record Graph(int vertices, int[][] edges) { }
    public record Approximation(int[] cover, int matchingSize) { }

    public static boolean satisfies(Clause[] clauses, boolean[] assignment) {
        for (Clause q : clauses) if (!literal(q.a, assignment) && !literal(q.b, assignment) && !literal(q.c, assignment)) return false;
        return true;
    }
    private static boolean literal(int lit, boolean[] x) { int v = Math.abs(lit)-1; return v >= 0 && v < x.length && (lit > 0 ? x[v] : !x[v]); }
    // Standard polynomial reductions from a 3-CNF formula.
    public static Graph toClique(Clause[] f) {
        int n = f.length * 3; int[][] e = new int[n*(n-1)/2][2]; int k=0;
        for (int i=0;i<n;i++) for(int j=i+1;j<n;j++) {
            int ci=i/3,cj=j/3, li=literalAt(f[ci],i%3), lj=literalAt(f[cj],j%3);
            if(ci!=cj && li!=-lj) e[k++]=new int[]{i,j};
        }
        return new Graph(n, Arrays.copyOf(e,k));
    }
    private static int literalAt(Clause c,int p){return p==0?c.a:p==1?c.b:c.c;}
    public static Graph toIndependentSet(Graph g) {
        int n=g.vertices, total=n*(n-1)/2; int[][] e=new int[total-g.edges.length][2]; boolean[][] has=new boolean[n][n];
        for(int[] x:g.edges) has[x[0]][x[1]]=has[x[1]][x[0]]=true;
        int k=0; for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(!has[i][j])e[k++]=new int[]{i,j}; return new Graph(n,e);
    }
    public static Graph toVertexCover(Graph g) { return new Graph(g.vertices, copyEdges(g.edges)); }
    private static int[][] copyEdges(int[][] e){int[][] c=new int[e.length][2];for(int i=0;i<e.length;i++)c[i]=e[i].clone();return c;}
    public static Approximation vertexCover2Approx(Graph g) {
        boolean[] used=new boolean[g.vertices], cover=new boolean[g.vertices]; int matching=0;
        for(int[] e:g.edges)if(!used[e[0]]&&!used[e[1]]){used[e[0]]=used[e[1]]=cover[e[0]]=cover[e[1]]=true;matching++;}
        int n=0;for(boolean b:cover)if(b)n++;int[] ans=new int[n];for(int i=0,k=0;i<cover.length;i++)if(cover[i])ans[k++]=i;return new Approximation(ans,matching);
    }

    public static void quickSort(int[] a){quickSort(a,0,a.length-1,new Random());}
    private static void quickSort(int[] a,int lo,int hi,Random random){while(lo<hi){int p=partition(a,lo,hi,random);if(p-lo<hi-p){quickSort(a,lo,p-1,random);lo=p+1;}else{quickSort(a,p+1,hi,random);hi=p-1;}}}
    private static int partition(int[] a,int lo,int hi,Random random){int chosen=lo+random.nextInt(hi-lo+1),swap=a[chosen];a[chosen]=a[hi];a[hi]=swap;int pivot=a[hi],i=lo;for(int j=lo;j<hi;j++)if(a[j]<=pivot){int t=a[i];a[i++]=a[j];a[j]=t;}int t=a[i];a[i]=a[hi];a[hi]=t;return i;}
    public static int reservoirSample(int[] stream,long seed){if(stream.length==0)throw new IllegalArgumentException("empty stream");Random r=new Random(seed);int chosen=stream[0];for(int i=1;i<stream.length;i++)if(r.nextInt(i+1)==0)chosen=stream[i];return chosen;}
    public static boolean isPrime(long n){if(n<2)return false;for(int p:new int[]{2,3,5,7,11,13,17,19,23,29,31,37}){if(n%p==0)return n==p;}long d=n-1;int s=0;while((d&1)==0){d>>=1;s++;}for(long a:new long[]{2,325,9375,28178,450775,9780504,1795265022}){if(a%n==0)continue;long x=powMod(a%n,d,n);if(x==1||x==n-1)continue;boolean witness=true;for(int r=1;r<s;r++){x=mulMod(x,x,n);if(x==n-1){witness=false;break;}}if(witness)return false;}return true;}
    private static long powMod(long a,long e,long m){long r=1;while(e>0){if((e&1)!=0)r=mulMod(r,a,m);a=mulMod(a,a,m);e>>=1;}return r;}
    // Overflow-safe for positive signed-long moduli; repeated doubling keeps every value below m.
    private static long mulMod(long a,long b,long m){long r=0;while(b>0){if((b&1)!=0)r=r>=m-a?r-(m-a):r+a;a=a>=m-a?a-(m-a):a+a;b>>=1;}return r;}
    public static long[] blellochExclusiveScan(long[] a){int n=1;while(n<a.length)n<<=1;long[] x=new long[n];System.arraycopy(a,0,x,0,a.length);for(int d=1;d<n;d<<=1)for(int i=(d<<1)-1;i<n;i+=d<<1)x[i]+=x[i-d];if(n>0)x[n-1]=0;for(int d=n>>1;d>=1;d>>=1)for(int i=(d<<1)-1;i<n;i+=d<<1){long t=x[i-d];x[i-d]=x[i];x[i]+=t;}return Arrays.copyOf(x,a.length);}
    public static long parallelReduce(long[] a,LongBinaryOperator op,int workers){if(a.length==0)throw new IllegalArgumentException("empty input");int chunks=Math.max(1,Math.min(workers,a.length));long[] part=new long[chunks];Thread[] ts=new Thread[chunks];for(int c=0;c<chunks;c++){final int id=c,start=c*a.length/chunks,end=(c+1)*a.length/chunks;ts[c]=new Thread(()->{long v=a[start];for(int i=start+1;i<end;i++)v=op.applyAsLong(v,a[i]);part[id]=v;});ts[c].start();}try{for(Thread t:ts)t.join();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}long v=part[0];for(int i=1;i<chunks;i++)v=op.applyAsLong(v,part[i]);return v;}
}
