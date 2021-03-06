% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/MiSeal.R
\name{wUpdate}
\alias{wUpdate}
\title{Update W vector.}
\usage{
wUpdate(W, theta, distancesBetweenPoints, trend, pW, hc)
}
\arguments{
\item{W}{the current label vector.}

\item{theta}{a vector containing the current parameters.}

\item{distancesBetweenPoints}{a matrix containing the pairwise distances between all points of the minutiae pattern.}

\item{trend}{a vector containing the intensity of necessary minutiae in the minutiae points.}

\item{pW}{the success probability of the Bernoulli trial.}

\item{hc}{the hard core distance.}
}
\value{
A 2D list where the first entry contains the \eqn{W} vector after applying a Hastings step. The second entry indicates whether the (newly) proposed vector \eqn{W'} was accepted (1) or rejected (0).
}
\description{
Update W vector.
}
