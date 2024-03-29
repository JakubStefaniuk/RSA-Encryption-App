package rsaapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.StringTokenizer;

/*
   
   This class provides static methods, all of them used in RSA process, also 
   TwoThreeTuple class definition to return more than one long value.
   Note that this implementation is not perfect, the main task was to represent 
   the RSA process, value types used here are mainly long, for safer use Big 
   Integer should be used. The primes binary file joined to these source files 
   consists of primes that are smaller than 15 mln, much smaller than those in 
   a real cryptography. Also the algorithm used in chaning characters into number
   - here used making a long value, which consists of ASCII numbers - 2 digits for 
   each character, as 22 is substracted. 
   Example:
   aB C ---ASCII--->(97)(66)(32)(67) --(-22)-->(75)(44)(10)(45)
   Similar reverse process used in decrypting.

   Helpful sources used:
   https://primes.utm.edu/lists/small/millions/ - sample list of primes
   

*/

public class RSA {
    //optimized approach to compute (a^b) % c
    private static long fastExpoMod(long a,long b,long c) {
    BigInteger x = new BigInteger("1");
    BigInteger y = new BigInteger(Long.toString(a));
    while(b > 0){
        if(b%2 == 1){
            x=(x.multiply(y)).mod(new BigInteger(Long.toString(c)));
        }
        y = (y.multiply(y)).mod(new BigInteger(Long.toString(c))); 
        b /= 2;
    }
    return (x.mod(new BigInteger(Long.toString(c)))).longValue();
}
    //randomly chooses 2 primes from binary file fileName
    public static TwoThreeTuple generatePrimes(String fileName){
        long fLength,rand1=0L,rand2=0L,num1=0L,num2=0L;
        try {
            RandomAccessFile RAF = new RandomAccessFile(new File(fileName),"r");
            fLength=(long)RAF.length()/Long.BYTES;
            rand1=(long)(Math.random()*fLength);
            rand2=(long)(Math.random()*fLength);
            RAF.seek(rand1*Long.BYTES);
            num1=RAF.readLong();
            RAF.seek(rand2*Long.BYTES);
            num2=RAF.readLong();
        } 
        catch (FileNotFoundException ex) {
            ex.getMessage();
        }
        catch(IOException ex){
            ex.getMessage();
        }
        return new TwoThreeTuple(num1,num2);
    }
    //returns greatest common divisor of a and b
    public static long GCD(long a, long b){
        if(a>b){
            if(b==0){
                return a;
            }
            else return GCD(b,a%b);
        }
        else{
            if(a==0)
                return b;
            else return GCD(a,b%a);
        }
    }
    //computes number b such, that (a*b)%n=1
    public static long modInv(long a, long n)
        {
          long a0,n0,p0,p1,q,r,t;

          p0 = 0; p1 = 1; a0 = a; n0 = n;
          q  = n0 / a0;
          r  = n0 % a0;
          while(r > 0)
          {
            t = p0 - q * p1;
            if(t >= 0)
              t = t % n;
            else
              t = n - ((-t) % n);
            p0 = p1; p1 = t;
            n0 = a0; a0 = r;
            q  = n0 / a0;
            r  = n0 % a0;
          }
          return p1;
        }
    //gerenerates Keys using function generatePrimes, inidcations used as in quite
    //every RSA description
    public static TwoThreeTuple generateKeys(){
        long n,d=0,e=0,EulerVal;
        long tmp=0;
        TwoThreeTuple primes = generatePrimes("primesbinary.bin");
        n=primes.getFirst().longValue()*primes.getSecond().longValue();
        EulerVal=(primes.getFirst().longValue()-1)*(primes.getSecond().longValue()-1);
        //choosing e, odd and such that gcd(e,EulerVal)=1
        do{
            e=(long)(Math.random()*n);
            tmp=GCD(EulerVal,e);
        }
        while(tmp!=1||((e&1)==0));
        d=modInv(e,EulerVal);
        return new TwoThreeTuple(d,e,n);
    }
    //encrypting with public key (e,n)
    public static String encrypt(String msg, long e, long n){
        StringBuilder encMsg = new StringBuilder();
        int i,j,k;
        long tmp=0;
        //changing message into numbers
        for(i = 0; i < msg.length()/4; i++){
            tmp=(msg.charAt(4*i)-22)*(long)Math.pow(10, 0);
            tmp+=(msg.charAt(4*i+1)-22)*(long)Math.pow(10, 2);
            tmp+=(msg.charAt(4*i+2)-22)*(long)Math.pow(10, 4);
            tmp+=(msg.charAt(4*i+3)-22)*(long)Math.pow(10, 6);
            long enc4b = fastExpoMod(tmp,e,n);
            encMsg.append(enc4b);
            if((4*i+3)!=msg.length()-1)
            encMsg.append('|');
        }
        tmp=0;
        for(j = i*4, k=0; j < msg.length();j++,k++){
            tmp+=(msg.charAt(j)-22)*(long)Math.pow(10, 2*k);
        }
        if(tmp!=0){
            long enc4b = fastExpoMod(tmp,e,n);
            encMsg.append(enc4b);
        }
        return encMsg.toString();
    }
    //decrypting with private key (d,n)
    public static String decrypt(String encMsg, long d, long n){
        StringBuilder msg = new StringBuilder("");
        StringTokenizer tokens = new StringTokenizer(encMsg,"|");
        System.out.println(encMsg);
        while(tokens.hasMoreTokens()){
            long val = Long.parseLong(tokens.nextToken());
            String valDecr = Long.toString(fastExpoMod(val,d,n)); 
            System.out.println(valDecr.length()); 
            for(int i = 0; i < valDecr.length()/2;i++){
                String sign = valDecr.substring(valDecr.length()-2*(i+1), valDecr.length()-2*i);
                char c = (char)(Long.parseLong(sign)+22);
                msg.append(c);
            }
        }
        return msg.toString();
    }
    //nested class representing pair or triplet of numbers
    public static class TwoThreeTuple<T extends Number>{
        private final T first;
        private final T second;
        private final T third;
        
        public TwoThreeTuple(T first, T second){
            this.first=first;
            this.second=second;
            this.third=null;
        }
        public TwoThreeTuple(T first, T second, T third){
            this.first=first;
            this.second=second;
            this.third=third;
        }
        public T getSecond() {return second;}
        public T getFirst() {return first;}
        public T getThird() {return third;}
    }
}
