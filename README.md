Mahout-Demo by Geoffrey Lee (c) Tamber, Inc. 2014
=================================================

This is the code which I developed in order to build a basic impelmentation of an Apache Mahout User Based Recommender. The recommender is compared against Tamber's proprietary recommendation engine named Engie with regards to speed and accuracy. The comparison test is currently hosted at http://engie.org/index_new.html although there are plans to move this page to http://engie.org

Right now, Mahout is being trained on the Movie Lens database; the file that I have included is the 100k ratings file. The file which the demo hosted at engie.org is trained on is the 10M ratings file (if you want Mahout to parse the 10M on a medium instance you have to give the VM more heap space via command line).

You will notice that a user preference of 4.0 is hard-coded in for each movie ID submitted to the recommender; this is because this is the same information which we provide to Engie (for ease of user experience, we merely have users select movies rather than rate them out of 5 although this could easily be changed with a few lines of code; since both Engines are trained on the ML database which rates out of 5, this was our approximation for how to do a comparison). I may later upload a more flexible version that does not hard code in the user preference.  

If you want to run the local app, pass via command line a list of movie IDs separated by space. If you wish to run the server app, you must open port 24579 and execute the server app as a background service. You can pass it commands by writing to a socket. I have also uploaded a basic php script demonstrating this. 
