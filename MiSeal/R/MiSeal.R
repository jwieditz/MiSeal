#' Compute the ratio of proposals.
#'
#' @param theta a vector containing the current parameters.
#' @param thetaNew a vector containing the new parameter.
#' @param type the proposal used. Yet only log-normal proposal implemented.
#'
#' @return Ratio of proposals.

qRatio <- function( theta, thetaNew, type = "mv.lognormal" ){

  return( thetaNew[1] * thetaNew[2] / theta[1] / theta[2] )
}

#' Compute number of \eqn{r}-close pairs of points.
#'
#' @param r the interaction distance of the Strauss process.
#' @param W the current label vector.
#' @param distancesBetweenPoints a matrix containing the pairwise distances between all points of the point pattern.
#'
#' @return Number of \eqn{r}-close pairs of points.

sR <- function( r, W, distancesBetweenPoints ){

  # the second condition is for not counting the point itself
  return( length(which(distancesBetweenPoints[ which(W==1), which(W==1) ] < r & distancesBetweenPoints[ which(W==1), which(W==1) ] > 0)) / 2)
}

#' Compute number of \eqn{r}-close neighbours of \eqn{z_i}.
#'
#' @param r the interaction distance of the Strauss process.
#' @param W the current label vector.
#' @param distancesBetweenPoints a matrix containing the pairwise distances between all points of the point pattern.
#' @param index the index of the considered point \eqn{z_i}.
#'
#' @return Number of r-close neighbours of \eqn{z_i}.

tR <- function( r, W, distancesBetweenPoints, index ){

  return( length(which(distancesBetweenPoints[index, which(W==1)] < r & distancesBetweenPoints[index, which(W==1)] > 0)) )
}

#' Calculate ratio of posteriors.
#'
#' @param theta a vector containing the current parameters.
#' @param thetaNew a vector containing the new parameter.
#' @param auxiliaryPattern a point pattern (\code{spatstat ppp}) containing the current auxiliary point pattern corresponding to theta.
#' @param auxiliaryPatternDistances a matrix containing the pairwise distances between all points of the auxiliary point pattern.
#' @param W the current label vector.
#' @param window the region of interest window of the point pattern.
#' @param nrep the maximum number of interactions used for generating the new (proposed) auxiliary point pattern from the Strauss process.
#' @param intensityImage a pixel image (\code{spatstat im}) containing the intensity of necessary minutiae.
#' @param thetaHat the maximum pseudo likelihood estimate from the burn in.
#' @param distancesBetweenPoints a matrix containing the pairwise distances between all points of the minutiae pattern.
#' @param hc the hard core distance.
#'
#' @return Ratio of the posterior likelihoods for the proposed model at the current iteration.

posteriorRatio <- function( theta, thetaNew, auxiliaryPattern, auxiliaryPatternDistances, W, window, nrep = 1e6, intensityImage, thetaHat, distancesBetweenPoints, hc ){

  numberOfOnes  <- sum(W)
  vol           <- volume(window)

  # ## generate auxiliary Strauss pattern depending on the value of the interaction parameter gamma_new
  # simulate point pattern using MH
  model             <- list(cif = "straush", par = list(beta = thetaNew[1], gamma = thetaNew[2], r = thetaNew[3], hc = hc), w = window, trend = intensityImage)

  auxiliaryPatternNew           <- rmh(model = model, control = list(nrep = nrep), verbose = FALSE )
  auxiliaryPatternNewDistances  <- pairdist(auxiliaryPatternNew)

  ## Hastings ratio for point pattern with auxiliary points
  sRAuxiliaryPattern      <- sR(theta[3], rep(1, auxiliaryPattern$n), auxiliaryPatternDistances)
  sRAuxiliaryPatternNew   <- sR(thetaNew[3], rep(1, auxiliaryPatternNew$n), auxiliaryPatternNewDistances)
  factor1        <- (thetaHat[1])^(auxiliaryPatternNew$n - auxiliaryPattern$n) * (thetaHat[2])^( sRAuxiliaryPatternNew - sRAuxiliaryPattern  )
  factor2        <- (theta[1])^(auxiliaryPattern$n) / (thetaNew[1])^(auxiliaryPatternNew$n) * (thetaNew[1] / theta[1])^numberOfOnes
  factor3        <- (thetaNew[2] / theta[2])^( sR(theta[3], W, distancesBetweenPoints) ) * (theta[2]^sRAuxiliaryPattern) / (thetaNew[2]^sRAuxiliaryPatternNew)
  hastingsRatio         <- factor1 * factor2 * factor3

  return( list(hastingsRatio, auxiliaryPatternNew, auxiliaryPatternNewDistances, factor1, factor2, factor3) )
}

#' Update parameter vector.
#'
#' @param theta a vector containing the current parameters.
#' @param auxiliaryPattern a point pattern (\code{spatstat ppp}) containing the current auxiliary point pattern corresponding to theta.
#' @param auxiliaryPatternDistances a matrix containing the pairwise distances between all points of the auxiliary point pattern.
#' @param W the current label vector.
#' @param window the region of interest window of the point pattern.
#' @param intensityPrior a vector containing the parameters \eqn{a,b,m} for the prior of the intensity parameter beta (Gamma distribution with parameters \eqn{a / m}, \eqn{b / m}).
#' @param  interactionPrior a vector containing the parameters \eqn{p,q} for the prior of the interaction parameter gamma (Beta distribution with parameters \eqn{a} and \eqn{b}).
#' @param noisePrior a vector containing the parameters \eqn{a,b} for the prior for the intensity of the random minutiae (Gamma distribution with parameters \eqn{a} and \eqn{b}).
#' @param intensityImage a pixel image (\code{spatstat im}) containing the intensity of necessary minutiae.
#' @param thetaHat the maximum pseudo likelihood estimate from the burn in.
#' @param distancesBetweenPoints a matrix containing the pairwise distances between all points of the minutiae pattern.
#' @param hc the hard core distance.
#' @param Sigma the covariance matrix of the parameters beta and gamma.
#' @param nrep the maximum number of interactions used for generating the new (proposed) auxiliary point pattern from the Strauss process.
#'
#' @return List containing the updated parameter vector (if discarded the old one), the corresponding auxiliary pattern, their pairwise distance matrix. Moreover, we return the index of the updated parameter, whether the proposal was accepted (1 for accept, 0 for reject) and the old auxiliary pattern.

thetaUpdate <- function( theta, auxiliaryPattern, auxiliaryPatternDistances, W, window, intensityPrior, interactionPrior, noisePrior, intensityImage, thetaHat, distancesBetweenPoints, hc, Sigma, nrep ){

  numberOfOnes  <- sum(W)
  numberOfZeros <- length(W) - numberOfOnes
  vol           <- volume(window)
  i             <- sample(1:2, size = 1, prob = c(.8, .2))
  thetaNew      <- theta

  if( i == 1 ){

    intensityPriorAlpha   <- intensityPrior[1]
    intensityPriorBeta    <- intensityPrior[2]
    betaMean              <- intensityPrior[3]

    interactionPriorAlpha <- interactionPrior[1]
    interactionPriorBeta  <- interactionPrior[2]

    repeat{
      thetaNew[1:2]             <- exp( mvrnorm( n = 1, mu = log(theta[1:2]), Sigma = Sigma ))

      if( thetaNew[2] <= 1 ){
        break
      }
    }

    beta      <- theta[1]
    gamma     <- theta[2]

    betaNew   <- thetaNew[1]
    gammaNew  <- thetaNew[2]

    priorRatio <- (gammaNew / gamma)^(interactionPriorAlpha - 1) * ( ( 1 - gammaNew ) / ( 1 - gamma ) )^(interactionPriorBeta - 1) * (betaNew / beta)^(intensityPriorAlpha - 1) * exp(- intensityPriorBeta / betaMean * (betaNew - beta))

    ## Compute Hastings ratio. The last term comes from the Gamma prior.
    L                       <- posteriorRatio(theta, thetaNew, auxiliaryPattern, auxiliaryPatternDistances, W, window, nrep = nrep, intensityImage = intensityImage, thetaHat = thetaHat, distancesBetweenPoints = distancesBetweenPoints, hc = hc)
    hastingsRatio           <- L[[1]] * qRatio(theta, thetaNew, type = "mv.lognormal") * priorRatio

  } else {

    noisePriorAlpha       <- noisePrior[1]
    noisePriorBeta        <- noisePrior[2]
    thetaNew[4]           <- rgamma( 1, shape = noisePriorAlpha + numberOfZeros , rate = noisePriorBeta + vol )
    hastingsRatio         <- 1

  }

  if( !is.na(hastingsRatio) && runif(1, min = 0, max = 1) < hastingsRatio ){

    if( i!= 2 ){

      return( list( thetaNew, L[[2]], L[[3]], i, 1, auxiliaryPattern ) )
    }
    else{

      return( list( thetaNew, auxiliaryPattern, auxiliaryPatternDistances, i, 1, auxiliaryPattern ) )
    }
  } else {

    return( list( theta, auxiliaryPattern, auxiliaryPatternDistances, i, 0, L[[2]] ) )
  }
}

#' Initialise data for algorithm.
#'
#' @details Initialise data for algorithm. Convention for input of priors: first two components are parameters of gamma distribution, third is defines a multiplicative constant.
#'
#' @param intensityPrior a vector containing the parameters \eqn{a,b,m} for the prior of the intensity parameter beta (Gamma distribution with parameters \eqn{a / m}, \eqn{b / m}).
#' @param  interactionPrior a vector containing the parameters \eqn{p,q} for the prior of the interaction parameter gamma (Beta distribution with parameters \eqn{a} and \eqn{b}).
#' @param rPrior the prior for the soft core distance. Default value is the pre-calculated value \eqn{r = 3 * hc}.
#' @param noisePrior a vector containing the parameters \eqn{a,b} for the prior for the intensity of the random minutiae (Gamma distribution with parameters \eqn{a} and \eqn{b}).
#'
#' @return A sample of the parameter vector \eqn{(beta, gamma, r, lambda)} from the prior.

thetaInitialise <- function( intensityPrior, interactionPrior, rPrior = r, noisePrior ){

  ## prior for beta: gamma prior around some value betaMean
  ## Gamma prior for intensity parameter
  intensityPriorAlpha   <- intensityPrior[1]
  intensityPriorBeta    <- intensityPrior[2]
  beta                  <- intensityPrior[3] * rgamma( n = 1, shape = intensityPriorAlpha, rate = intensityPriorBeta )

  ## prior for gamma
  interactionPriorAlpha <- interactionPrior[1]
  interactionPriorBeta  <- interactionPrior[2]
  gamma                 <- rbeta(1, shape1 = interactionPriorAlpha, shape2 = interactionPriorBeta)

  ## prior for r
  r <- rPrior

  ## prior for noise level lambda
  noisePriorAlpha       <- noisePrior[1]
  noisePriorBeta        <- noisePrior[2]
  lambda                <- rgamma( n = 1 , shape = noisePriorAlpha, rate = noisePriorBeta )

  return( c(beta, gamma, r, lambda) )
}

#' Initial assignment \eqn{W} of minutiae to the necessary and random sub-patterns.
#'
#' @param n length of the label vector
#' @param distribution distribution for drawing an initial label vector from. Yet only i.i.d. Bernoulli implemented.
#' @param pW the success probability of the Bernoulli trial.
#'
#' @return A \eqn{{0,1}} vector of length \eqn{n} as \eqn{n}-sample from a Bernoulli distribution.

wInitialise <- function( n , distribution = "ber", pW ){

  return( sample(0:1, n, replace = TRUE, prob = c(1 - pW, pW) ) )
}

#' Update W vector.
#'
#' @param W the current label vector.
#' @param theta a vector containing the current parameters.
#' @param distancesBetweenPoints a matrix containing the pairwise distances between all points of the minutiae pattern.
#' @param trend a vector containing the intensity of necessary minutiae in the minutiae points.
#' @param pW the success probability of the Bernoulli trial.
#' @param hc the hard core distance.
#'
#' @return A 2D list where the first entry contains the \eqn{W} vector after applying a Hastings step. The second entry indicates whether the (newly) proposed vector \eqn{W'} was accepted (1) or rejected (0).

wUpdate <- function(W, theta, distancesBetweenPoints, trend, pW, hc ){

  n <- length(W)

  ## choose i in 1,...,n randomly (uniformly)
  i <- sample(1:n, 1)

  ## calculate Hastings ratio
  if( W[i] == 1 ){

    if( tR( hc, W, distancesBetweenPoints, i) > 0 ){

      hastingsRatio <- 1

    } else {

      hastingsRatio <- theta[4] / theta[1] / trend[i] / (theta[2]^tR( theta[3], W, distancesBetweenPoints, i )) * (1 - pW) / pW
    }
  } else {

    ## Compute the number of points within the hard core radius. If any, reject proposed assignment.
    Wnew    <- W
    Wnew[i] <- 1

    if( tR( hc, Wnew, distancesBetweenPoints, i) > 0 ){

      hastingsRatio <- 0

    } else {

      hastingsRatio <- theta[1] * trend[i] * (theta[2]^tR( theta[3], W, distancesBetweenPoints, i )) / theta[4] * pW / (1 - pW)
    }
  }

  ## accept if Hastings ratio >= alpha ~ U(0,1) and adjust the number of points in each subprocess

  if( hastingsRatio >= runif(1, min = 0, max = 1) ){

    if( W[i] == 0 ){

      W[i] <- 1

    } else {

      W[i] <- 0
    }
    return(list(W, 1))

  } else {

    return(list(W, 0))
  }
}

#' Separate a minutiae pattern.
#'
#' @details Classify a given minutiae pattern into necessary and random minutiae. The minutiae pattern is assumed to be a superposition of two independent processes Xi and Eta where Xi is a homogeneous Poisson process and Eta is an inhomogeneous Strauss process with hard core.
#'
#' @param imagePath character string containing the path to the fingerprint image.
#' The image has to be a greyscale image, ideally binarised.
#' @param roiPath character string containing the path to the file containing the region of interest
#' The file should be a csv file of the same dimension as the used image. We use the convention that 1 denotes background (is not ROI) and 0 denotes foreground pixels (is ROI).
#' @param minutiaePath character string containing the path to the minutiae file.
#'  The minutiae template has to be a .txt file, written in the following fashion: \cr
#' image width \cr
#' image height \cr
#' image resolution \cr
#' number of minutiae \cr
#' x-coordinate1 y-coordinate1 orientation1 \cr
#' x-coordinate2 y-coordinate2 orientation2 \cr
#' x-coordinate3 y-coordinate3 orientation3 \cr
#' ...  \cr
#' @param T number of iterations of the MCMC algorithm excluding burn in time.
#' @param burnIn number of burn in iterations.
#'
#' @return A list containing the traces of the parameters \eqn{(beta, gamma, lambda)}, the distribution of \eqn{W}, the acceptance rates, the marginal posteriors of \eqn{W}, the used MPLE and the considered point pattern as \code{spatstat ppp} with corresponding intensity image.
#'
#' @examples
#' \donttest{
#' imagePath <- system.file("extdata", "FVC2002_DB2_IM_7_6_image.png", package = "MiSeal")
#' roiPath <- system.file("extdata", "FVC2002_DB2_IM_7_6_roi.csv", package = "MiSeal")
#' minutiaePath <- system.file("extdata", "FVC2002_DB2_IM_7_6_minutiae.txt", package = "MiSeal")
#' MiSealTest <- MiSeal( imagePath, roiPath, minutiaePath, T = 2000, burnIn = 1000)
#'
#' dev.new()
#' par(mfrow = c(2,2), mar = c(4, 4, 2, 2))
#' plot((na.omit(MiSealTest$beta)), type = "l", xlab = "iteration", ylab = "beta", main = "trace plot for prefactor beta")
#' abline(h = MiSealTest$thetaHat[1], lty = "dotted", col = "chartreuse4", lwd = 2)
#'
#' plot((na.omit(MiSealTest$gamma)), type = "l", xlab = "iteration", ylab = "gamma", main = "trace plot for interaction parameter gamma")
#' abline(h = MiSealTest$thetaHat[2], lty = "dotted", col = "chartreuse4", lwd = 2)
#'
#' plot((na.omit(MiSealTest$lambda)), type = "l", xlab = "iteration", ylab = "lambda", main = "trace plot for noise intensity lambda")
#' }
#' barplot( MiSealTest$marginalW, names.arg = 1:length(MiSealTest$marginalW), main = "posterior probabilities for minutiae being necessary", col = 2, ylim = c(0,1) )

MiSeal <- function( imagePath, roiPath, minutiaePath, T = 1E6, burnIn = 1E4 ){

  set.seed(05082020)
  print("Compute fingerprint image data...")

  imageName <- sub(pattern = "(.*)\\..*$", replacement = "\\1", basename(imagePath))

  resultsDirectory <- "~/MiSeal/"
  dir.create(file.path(resultsDirectory), showWarnings = FALSE)
  dir.create(file.path(resultsDirectory, "RidgeFrequency"), showWarnings = FALSE)
  dir.create(file.path(resultsDirectory, "FieldDivergence"), showWarnings = FALSE)
  dir.create(file.path(resultsDirectory, "RidgeDivergence"), showWarnings = FALSE)

  ridgeFrequencyPath  <- paste0(resultsDirectory, "RidgeFrequency/", imageName, ".csv")
  fieldDivergencePath <- paste0(resultsDirectory, "FieldDivergence/", imageName, ".csv")
  ridgeDivergencePath <- paste0(resultsDirectory, "RidgeDivergence/", imageName, ".csv")

  system( paste0("java -jar ", system.file("java", "fingerprint-0.0.1.jar", package = "MiSeal"), " --nogui -roi ", roiPath, " -o default -OdefaultIter=5 -r default -ro ", ridgeFrequencyPath ," -d default -DdefaultSmoothing=gauss -DdefaultSmoothingSize=61 -DdefaultSmoothingMean=0 -DdefaultSmoothingVariance=2500 -do ", fieldDivergencePath, " -LSmoothing=gauss -LSmoothingSize=61 -LSmoothingMean=0 -LSmoothingVariance=2500 -lo ", ridgeDivergencePath, " -pnh 10 -pnv 20 ", imagePath) )

  print("Prepare start of MCMC...")

  fieldDivergence     <- as.matrix(read.csv(fieldDivergencePath, header = FALSE, sep = ",", dec = "."))
  ridgeDivergence     <- as.matrix(read.csv(ridgeDivergencePath, header = FALSE, sep = ",", dec = "."))
  ridgeFrequency      <- as.matrix(read.csv(ridgeFrequencyPath, header = FALSE, sep = ",", dec = "."))
  regionOfInterest    <- t(as.matrix(read.csv(roiPath, header = FALSE, sep = ",", dec = ".")))
  window              <- owin(xrange = c(1, ncol(ridgeDivergence)), yrange = c(1, nrow(ridgeDivergence)), mask = !is.na(ridgeDivergence) & !is.na(fieldDivergence) & (regionOfInterest == 0))

  fieldDivergenceImage          <- as.im(fieldDivergence * ridgeFrequency, W = owin(xrange = c(1, ncol(fieldDivergence)), yrange = c(1, nrow(fieldDivergence))))
  ridgeDivergenceImage          <- as.im(ridgeDivergence, W = owin(xrange = c(1, ncol(ridgeDivergence)), yrange = c(1, nrow(ridgeDivergence))))
  ridgeFrequencyImage           <- as.im(ridgeFrequency, W = owin(xrange = c(1, ncol(ridgeFrequency)), yrange = c(1, nrow(ridgeFrequency))))
  Window(fieldDivergenceImage)  <- window
  Window(ridgeDivergenceImage)  <- window
  intensityImage                <- abs(fieldDivergenceImage + ridgeDivergenceImage)

  ## prior Poisson intensity and Strauss-hard core parameters and parameters for Strauss simulation
  lambda0           <- 1e-4
  hc                <- 1 / mean( ridgeFrequencyImage, na.rm = TRUE )
  r                 <- 3 * hc
  intensityPrior    <- c(5, 5, 1)           ## variance = a / (b^2)
  interactionPrior  <- c(2, 5)              ## variance = ab / (a+b)^2 / (a+b+1)
  noisePrior        <- c(5, 5 / lambda0)
  nrep              <- 1e5

  ## include minutiae point pattern
  n                 <- as.numeric(read_table2( minutiaePath, col_names = FALSE, col_types = cols(), skip = 3, n_max = 1, locale = locale(decimal_mark = ",", grouping_mark = ".")))
  data              <- data.matrix(read_table2( minutiaePath, col_names = FALSE, col_types = cols(), skip = 4, n_max = n))
  y                 <- ppp(data[,2], data[,1], window = window )

  n                       <- y$n
  distancesBetweenPoints  <- pairdist(y)
  trend                   <- intensityImage[coords(y)]

  ## sample prior modelParameters
  theta                   <- thetaInitialise( intensityPrior = intensityPrior, interactionPrior = interactionPrior, rPrior = r, noisePrior = noisePrior )

  model0                  <- list(cif="straush", par = list(beta = theta[1], gamma = theta[2], r = theta[3], hc = hc), w = window, trend = intensityImage)

  auxiliaryPattern            <- rmh(model = model0, control = list(nrep = nrep), verbose = FALSE )
  auxiliaryPatternDistances   <- pairdist(auxiliaryPattern)
  L                           <- list( theta, auxiliaryPattern, auxiliaryPatternDistances )
  L.auxiliaryPattern          <- list( coords(auxiliaryPattern) )

  #### prior parameter for Z
  ## intercept of regression line and pW
  pW      <- max( 1 - lambda0 * sum(!is.nan(ridgeDivergence)) / n, 0 )

  #### definition of modelParameters

  ## modelParameters matrix, containing values of beta (row 1), gamma(row 2), lambda(row 3)
  modelParameters           <- matrix( NaN, nrow = 3, ncol = T )
  numberOfNecessaryMinutiae <- matrix( NaN, nrow = 1, ncol = T )
  wBar                      <- rep(0, times = n)

  #### actual MCMC algorithm ####

  ## Define initial assignment of points to necessary and random process, respectively
  ## Draw initial configuration uniformly. Repeat, if the assignment to the Strauss-hard core Process is not feasible (hard core condition violated).
  repeat{
    W     <- wInitialise( n = n, distribution = "unif", pW = pW)
    model <- ppm(y[W==1], ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc), clipwin = window )
    tmp   <- quad.ppm(model, drop = TRUE )
    model <- ppm(tmp, ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc), subset = window, clipwin = window )

    if( length(model$problems) == 0 ){
      rm(tmp)
      break
    }
  }

  wDistribution        <- matrix( NaN, nrow = T, ncol = n )
  wDistribution[1, ]   <- W

  thetaHat        <- exp( coef(model) )
  burnInThetaHats <- c(thetaHat)

  ## model parameters for prior and proposals
  ## lognormal proposal parameter
  standardDeviationBeta   <- 0.07
  standardDeviationGamma  <- 0.05
  correlationBetaGamma    <- -0.7
  Sigma                   <- matrix( c(standardDeviationBeta^2, correlationBetaGamma * standardDeviationBeta * standardDeviationGamma, correlationBetaGamma * standardDeviationBeta * standardDeviationGamma, standardDeviationGamma^2), nrow = 2, ncol = 2 )
  pTheta                  <- 0.05

  acceptanceRate    <- 0
  wAcceptanceRate   <- 0
  numberOfWUpdates  <- 0

  ## progress bar
  print("Start MCMC...")
  print("MCMC running...")
  progressBar <- txtProgressBar(min = 1, max = 100, style = 3)

  set.seed(420)

  ## start the MCMC method
  for( iter in 1:(T + burnIn) ){

    if( (iter <= burnIn) && (iter%%1000 == 0) ){

      burnInThetaHats <- cbind(burnInThetaHats, exp( coef( ppm(y[W==1], ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc) ))))
      thetaHat <- rowMeans(burnInThetaHats)
    }

    # initially toss coin for either update parameter or characterisation vector Z
    if( rbinom( 1, 1, pTheta ) == 0 ){

      wTilde  <- wUpdate(W = W, theta = theta, distancesBetweenPoints = distancesBetweenPoints, trend = trend, pW = pW, hc = hc)
      W       <- wTilde[[1]]

      if( wTilde[[2]] ){
        wAcceptanceRate <- wAcceptanceRate + 1
      }

      if( iter > burnIn ){
        numberOfWUpdates                          <- numberOfWUpdates + 1
        wBar                                      <- wBar + W  # marginal probabilities for minutiae to be necessary
        numberOfNecessaryMinutiae[iter - burnIn]  <- sum(W)
      }

    } else {

      L                           <- thetaUpdate( L[[1]], L[[2]], L[[3]], W, window, intensityPrior, interactionPrior, noisePrior, intensityImage = intensityImage, thetaHat = thetaHat, distancesBetweenPoints = distancesBetweenPoints, hc = hc, Sigma = Sigma, nrep )
      theta                       <- L[[1]]
      auxiliaryPattern            <- L[[2]]
      auxiliaryPatternDistances   <- L[[3]]

      ## if proposal was accepted, update auxiliary pattern, otherwise take the old one
      if( L[[5]] == 1 ){
        L.auxiliaryPattern  <- c(L.auxiliaryPattern, list( c( old = coords(L[[6]]), new = coords(auxiliaryPattern)) ))

      } else {
        L.auxiliaryPattern  <- c(L.auxiliaryPattern, list( c( old = coords(auxiliaryPattern), new = coords(L[[6]])) ))
      }

      ## if theta was updated, increment the acceptance rate counter
      if( L[[4]]!= 2 ){
        acceptanceRate <- acceptanceRate + L[[5]]
      }
    }

    if( iter > burnIn ){
      modelParameters[1, iter - burnIn] <- L[[1]][1]  # beta
      modelParameters[2, iter - burnIn] <- L[[1]][2]  # gamma
      modelParameters[3, iter - burnIn] <- L[[1]][4]  # lambda
      wDistribution[iter - burnIn, ]    <- W
    }

    if( (iter * 100 / (T + burnIn)) == round(iter * 100 / (T + burnIn)) ){
      setTxtProgressBar(progressBar, (iter / (T + burnIn) * 100) )
    }

  }

  close(progressBar)
  print("MCMC run finished successfully.")

  acceptanceRate  <- acceptanceRate / (T - numberOfWUpdates)
  wAcceptanceRate <- wAcceptanceRate / numberOfWUpdates
  wBar            <- wBar / numberOfWUpdates

  return( list( beta = modelParameters[1, ], gamma = modelParameters[2, ], lambda = modelParameters[3,], wDistribution = wDistribution, acceptanceRate = acceptanceRate, wAcceptanceRate = wAcceptanceRate, marginalW = wBar, thetaHat = thetaHat, minutiaPattern = y, intensityImage = intensityImage ) )
}


#' Separate a homogeneous Poisson process and an inhomogeneous Strauss process with hard core.
#'
#' @details Apply the PostSeal (Poisson-Strauss Separating Algorithm) and separate a given point pattern. The point pattern is assumed to be a superposition of two independent processes Xi and Eta where Xi is a homogeneous Poisson process and Eta is an inhomogeneous Strauss process with hard core.
#'
#' @param pointPattern a point pattern of type \code{spatstat ppp}.
#' @param intensityImage a pixel image (type \code{spatstat im}) which fits in its size to the input point pattern.
#' @param interactionRadii a vector \eqn{(h, r)} containing the hard core radius \eqn{h} and the Strauss interaction radius \eqn{r}.
#' @param T number of iterations of the MCMC algorithm excluding burn in time.
#' @param burnIn number of burn in iterations.
#'
#' @return A list containing the traces of the parameters \eqn{(beta, gamma, lambda)}, the distribution of \eqn{W}, the acceptance rates, the marginal posteriors of \eqn{W}, the used MPLE and the considered point pattern as \code{spatstat ppp} with corresponding intensity image.
#'
#' @examples
#' \donttest{
#' data("PostSealExample")
#' interactionRadii <- c(8, 24)
#' PostSealTest <- PostSeal( pointPattern, intensityImage, interactionRadii, T = 2000, burnIn = 1000)
#'
#' dev.new()
#' par(mfrow = c(2,2), mar = c(4, 4, 2, 2))
#' plot((na.omit(PostSealTest$beta)), type = "l", xlab = "iteration", ylab = "beta", main = "trace plot for prefactor beta")
#' abline(h = PostSealTest$thetaHat[1], lty = "dotted", col = "chartreuse4", lwd = 2)
#'
#' plot((na.omit(PostSealTest$gamma)), type = "l", xlab = "iteration", ylab = "gamma", main = "trace plot for interaction parameter gamma")
#' abline(h = PostSealTest$thetaHat[2], lty = "dotted", col = "chartreuse4", lwd = 2)
#'
#' plot((na.omit(PostSealTest$lambda)), type = "l", xlab = "iteration", ylab = "lambda", main = "trace plot for noise intensity lambda")
#' }
#' barplot( PostSealTest$marginalW, names.arg = 1:length(PostSealTest$marginalW), main = "posterior probabilities for minutiae being Strauss", col = 2, ylim = c(0,1) )

PostSeal <- function( pointPattern, intensityImage, interactionRadii, T = 1E6, burnIn = 1E4 ){

  set.seed(05082020)

  print("Prepare start of MCMC...")
  window              <- Window(intensityImage)
  intensityImage      <- intensityImage

  ## prior Poisson intensity and Strauss-hard core parameters and parameters for Strauss simulation
  lambda0           <- 1e-4
  hc                <- interactionRadii[1]
  r                 <- interactionRadii[2]
  intensityPrior    <- c(5, 5, 1)           ## variance = a / (b^2)
  interactionPrior  <- c(2, 5)              ## variance = ab / (a+b)^2 / (a+b+1)
  noisePrior        <- c(5, 5 / lambda0)
  nrep              <- 1e5

  ## include minutiae point pattern
  y         <- pointPattern
  Window(y) <- window

  n                       <- y$n
  distancesBetweenPoints  <- pairdist(y)
  trend                   <- intensityImage[coords(y)]

  ## sample prior modelParameters
  theta                   <- thetaInitialise( intensityPrior = intensityPrior, interactionPrior = interactionPrior, rPrior = r, noisePrior = noisePrior )

  model0                  <- list(cif="straush", par = list(beta = theta[1], gamma = theta[2], r = theta[3], hc = hc), w = window, trend = intensityImage)

  auxiliaryPattern            <- rmh(model = model0, control = list(nrep = nrep), verbose = FALSE )
  auxiliaryPatternDistances   <- pairdist(auxiliaryPattern)
  L                           <- list( theta, auxiliaryPattern, auxiliaryPatternDistances )
  L.auxiliaryPattern          <- list( coords(auxiliaryPattern) )

  #### prior parameter for Z
  ## intercept of regression line and pW
  pW      <- max( 1 - lambda0 * area(intensityImage) / n, 0 )

  #### definition of modelParameters

  ## modelParameters matrix, containing values of beta (row 1), gamma(row 2), lambda(row 3)
  modelParameters           <- matrix( NaN, nrow = 3, ncol = T )
  numberOfStraussPoints     <- matrix( NaN, nrow = 1, ncol = T )
  wBar                      <- rep(0, times = n)

  #### actual MCMC algorithm ####

  ## Define initial assignment of points to necessary and random process, respectively
  ## Draw initial configuration uniformly. Repeat, if the assignment to the Strauss-hard core Process is not feasible (hard core condition violated).
  repeat{
    W     <- wInitialise( n = n, distribution = "unif", pW = pW)
    model <- ppm(y[W==1], ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc), clipwin = window )
    tmp   <- quad.ppm(model, drop = TRUE )
    model <- ppm(tmp, ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc), subset = window, clipwin = window )

    if( length(model$problems) == 0 ){
      rm(tmp)
      break
    }
  }

  wDistribution        <- matrix( NaN, nrow = T, ncol = n )
  wDistribution[1, ]   <- W

  thetaHat        <- exp( coef(model) )
  burnInThetaHats <- c(thetaHat)

  ## model parameters for prior and proposals
  ## lognormal proposal parameter
  standardDeviationBeta   <- 0.07
  standardDeviationGamma  <- 0.05
  correlationBetaGamma    <- -0.7
  Sigma                   <- matrix( c(standardDeviationBeta^2, correlationBetaGamma * standardDeviationBeta * standardDeviationGamma, correlationBetaGamma * standardDeviationBeta * standardDeviationGamma, standardDeviationGamma^2), nrow = 2, ncol = 2 )
  pTheta                  <- 0.05

  acceptanceRate    <- 0
  wAcceptanceRate   <- 0
  numberOfWUpdates  <- 0

  ## progress bar
  print("Start MCMC...")
  print("PostSeal running...")
  progressBar <- txtProgressBar(min = 1, max = 100, style = 3)

  set.seed(420)

  ## start the MCMC method
  for( iter in 1:(T + burnIn) ){

    if( (iter <= burnIn) && (iter%%1000 == 0) ){

      burnInThetaHats <- cbind(burnInThetaHats, exp( coef( ppm(y[W==1], ~ offset(log(intensityImage)), interaction = StraussHard(r = theta[3], hc = hc) ))))
      thetaHat <- rowMeans(burnInThetaHats)
    }

    # initially toss coin for either update parameter or characterisation vector Z
    if( rbinom( 1, 1, pTheta ) == 0 ){

      wTilde  <- wUpdate(W = W, theta = theta, distancesBetweenPoints = distancesBetweenPoints, trend = trend, pW = pW, hc = hc)
      W       <- wTilde[[1]]

      if( wTilde[[2]] ){
        wAcceptanceRate <- wAcceptanceRate + 1
      }

      if( iter > burnIn ){
        numberOfWUpdates                          <- numberOfWUpdates + 1
        wBar                                      <- wBar + W  # marginal probabilities for minutiae to be necessary
        numberOfStraussPoints[iter - burnIn]  <- sum(W)
      }

    } else {

      L                           <- thetaUpdate( L[[1]], L[[2]], L[[3]], W, window, intensityPrior, interactionPrior, noisePrior, intensityImage = intensityImage, thetaHat = thetaHat, distancesBetweenPoints = distancesBetweenPoints, hc = hc, Sigma = Sigma, nrep )
      theta                       <- L[[1]]
      auxiliaryPattern            <- L[[2]]
      auxiliaryPatternDistances   <- L[[3]]

      ## if proposal was accepted, update auxiliary pattern, otherwise take the old one
      if( L[[5]] == 1 ){
        L.auxiliaryPattern  <- c(L.auxiliaryPattern, list( c( old = coords(L[[6]]), new = coords(auxiliaryPattern)) ))

      } else {
        L.auxiliaryPattern  <- c(L.auxiliaryPattern, list( c( old = coords(auxiliaryPattern), new = coords(L[[6]])) ))
      }

      ## if theta was updated, increment the acceptance rate counter
      if( L[[4]]!= 2 ){
        acceptanceRate <- acceptanceRate + L[[5]]
      }
    }

    if( iter > burnIn ){
      modelParameters[1, iter - burnIn] <- L[[1]][1]  # beta
      modelParameters[2, iter - burnIn] <- L[[1]][2]  # gamma
      modelParameters[3, iter - burnIn] <- L[[1]][4]  # lambda
      wDistribution[iter - burnIn, ]    <- W
    }

    if( (iter * 100 / (T + burnIn)) == round(iter * 100 / (T + burnIn)) ){
      setTxtProgressBar(progressBar, (iter / (T + burnIn) * 100) )
    }

  }

  close(progressBar)
  print("MCMC run finished successfully.")

  acceptanceRate  <- acceptanceRate / (T - numberOfWUpdates)
  wAcceptanceRate <- wAcceptanceRate / numberOfWUpdates
  wBar            <- wBar / numberOfWUpdates

  return( list( beta = modelParameters[1, ], gamma = modelParameters[2, ], lambda = modelParameters[3,], wDistribution = wDistribution, acceptanceRate = acceptanceRate, wAcceptanceRate = wAcceptanceRate, marginalW = wBar, thetaHat = thetaHat, pointPattern = y, intensityImage = intensityImage ) )
}
