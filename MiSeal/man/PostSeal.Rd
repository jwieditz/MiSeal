% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/MiSeal.R
\name{PostSeal}
\alias{PostSeal}
\title{Separate a homogeneous Poisson process and an inhomogeneous Strauss process with hard core.}
\usage{
PostSeal(
  pointPattern,
  intensityImage,
  interactionRadii,
  T = 1e+06,
  burnIn = 10000
)
}
\arguments{
\item{pointPattern}{a point pattern of type \code{spatstat ppp}.}

\item{intensityImage}{a pixel image (type \code{spatstat im}) which fits in its size to the input point pattern.}

\item{interactionRadii}{a vector \eqn{(h, r)} containing the hard core radius \eqn{h} and the Strauss interaction radius \eqn{r}.}

\item{T}{number of iterations of the MCMC algorithm excluding burn in time.}

\item{burnIn}{number of burn in iterations.}
}
\value{
A list containing the traces of the parameters \eqn{(beta, gamma, lambda)}, the distribution of \eqn{W}, the acceptance rates, the marginal posteriors of \eqn{W}, the used MPLE and the considered point pattern as \code{spatstat ppp} with corresponding intensity image.
}
\description{
Separate a homogeneous Poisson process and an inhomogeneous Strauss process with hard core.
}
\details{
Apply the PostSeal (Poisson-Strauss Separating Algorithm) and separate a given point pattern. The point pattern is assumed to be a superposition of two independent processes Xi and Eta where Xi is a homogeneous Poisson process and Eta is an inhomogeneous Strauss process with hard core.
}
\examples{
\donttest{
data("PostSealExample")
interactionRadii <- c(8, 24)
PostSealTest <- PostSeal( pointPattern, intensityImage, interactionRadii, T = 2000, burnIn = 1000)

dev.new()
par(mfrow = c(2,2), mar = c(4, 4, 2, 2))
plot((na.omit(PostSealTest$beta)), type = "l", xlab = "iteration", ylab = "beta", main = "trace plot for prefactor beta")
abline(h = PostSealTest$thetaHat[1], lty = "dotted", col = "chartreuse4", lwd = 2)

plot((na.omit(PostSealTest$gamma)), type = "l", xlab = "iteration", ylab = "gamma", main = "trace plot for interaction parameter gamma")
abline(h = PostSealTest$thetaHat[2], lty = "dotted", col = "chartreuse4", lwd = 2)

plot((na.omit(PostSealTest$lambda)), type = "l", xlab = "iteration", ylab = "lambda", main = "trace plot for noise intensity lambda")
}
barplot( PostSealTest$marginalW, names.arg = 1:length(PostSealTest$marginalW), main = "posterior probabilities for minutiae being Strauss", col = 2, ylim = c(0,1) )
}
