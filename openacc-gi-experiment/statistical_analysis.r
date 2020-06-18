#!/usr/bin/env Rscript

args <- commandArgs(trailingOnly = TRUE)

original <- read.csv(args[1])
optimal <- read.csv(args[2])

print("Original mean (seconds):", quote = FALSE)
mean(original$seconds)

print("", quote = FALSE)

print("Optimal mean (seconds):", quote = FALSE)
mean(optimal$seconds)

print("", quote = FALSE)

print("Optimal standard deviation:", quote = FALSE)
sd(optimal$seconds)

print("", quote = FALSE)

original_error <- qnorm(0.975) * sd(original$seconds) /sqrt(length(original$seconds))

print("Original 95% confidence interval upper-bound:", quote = FALSE)
mean(original$seconds) + original_error

print("Original 95% confidence interval lower-bound:", quote = FALSE)
mean(original$seconds) - original_error

print("", quote = FALSE)

optimal_error <- qnorm(0.975) * sd(optimal$seconds) / sqrt(length(optimal$seconds))

print("Optimal 95% confidence interval upper-bound:", quote = FALSE)
mean(optimal$seconds) + optimal_error

print("Optimal 95% confidence interval lower-bound:", quote = FALSE)
mean(optimal$seconds) - optimal_error

print("", quote = FALSE)

wilcox.test(original$seconds, optimal$seconds)
