# Molecular Properties

Cheminformatics is about molecular properties and chemistry in general the field
of finding chemicals with new properties. Prof. Gasteiger in 2006 gave a
lecture at Cologne University where he expressed this view. It stuck around. We keep
databases to store those properties, and we develop methods to predict and understand
those properties. Prediction is important for one reason: there are too many
chemical structures and we cannot experimentally measure the properties for all
of them. The number of molecules is often said to be relevant to drug discovery is in
the order of $10^{60}$. The largest current databases have less than $10^{8}$
structures. That means that prediction of properties for the vast majority
of molecules will remain relevant for the foreseeable future.

This chapter will show how the CDK can be used to calculate a number of molecular
properties.

## Molecular Mass

The simplest but perhaps the most reported molecular property is the <a name="tp1">molecular mass</a>.
It is important to realize this mass is not constant, and depends on the natural
mixture of isotopes, which is not constant itself. If you have an atom container
with explicit hydrogens, you can loop over the atoms to calculate the molecular
mass as summation of the masses of the individual atoms:

**Script** [code/CalculateMolecularWeight.groovy](code/CalculateMolecularWeight.code.md)
```groovy
molWeight = 0.0
for (atom in molecule.atoms()) {
  molWeight += isotopeInfo.getNaturalMass(atom)
}
```

In this case, you can also use the [`AtomContainerManipulator`](http://cdk.github.io/cdk/latest/docs/api/org/openscience/cdk/tools/manipulator/AtomContainerManipulator.html):

**Script** [code/CalculateMolecularWeightShort.groovy](code/CalculateMolecularWeightShort.code.md)
```groovy
molWeight = AtomContainerManipulator
  .getNaturalExactMass(molecule)
```

The element masses are calculated from the accurate isotope masses and natural
abundances defined in the Blue Obelisk Data Repository [<a href="#citeref1">1</a>].

### Implicit Hydrogens

If your atom container has <a name="tp2">implicit hydrogens</a> specified, you will have the above
code will not be sufficient. Instead, your code should look like:

**Script** [code/CalculateMolecularWeightImplicitHydrogens.groovy](code/CalculateMolecularWeightImplicitHydrogens.code.md)
```groovy
molWeight = 0.0
hWeight = isotopeInfo.getNaturalMass(Elements.HYDROGEN)
for (atom in molecule.atoms()) {
  molWeight += isotopeInfo.getNaturalMass(atom)
  if (atom.getImplicitHydrogenCount() != CDKConstants.UNSET)
    molWeight += atom.getImplicitHydrogenCount() *
                 hWeight
}
```

<a name="sec:tpsa"></a>
## Total Polar Surface Area

Another properties that frequently returns in cheminformatics is the <a name="tp3">Total Polar Surface Area</a>
(<a name="tp4">TPSA</a>). The code in the CDK uses an algorithm published by Ertl in 2000 [<a href="#citeref2">2</a>].
Here too, the descriptor API is used, so that the code is quite similar to that for the logP
calculation:

**Script** [code/TPSA.groovy](code/TPSA.code.md)
```groovy
oxazone = MoleculeFactory.makeOxazole();
benzene = MoleculeFactory.makeBenzene();
// add explicit hydrogens ...
descriptor = new TPSADescriptor()
println "TPSA of oxazone: " +
  ((DoubleResult)descriptor.calculate(oxazone).value)
  .doubleValue()
println "TPSA of benzene: " +
  ((DoubleResult)descriptor.calculate(benzene).value)
  .doubleValue()
```

which returns:

```plain
TPSA of oxazone: 21.59
TPSA of benzene: 0.0
```

## References

1. <a name="citeref1"></a>Missing
2. <a name="citeref2"></a>Missing
