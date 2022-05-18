target:
	@$(MAKE) pr

dev:
	pip3 install --upgrade pip pipenv
	pipenv install --dev --pre
	pipenv run pip install -U pip
	pip3 install --upgrade pre-commit
	pre-commit install
	pre-commit install --hook-type commit-msg

pre-commit:
	pre-commit run --show-diff-on-failure

format:
	pipenv run isort src tests
	pipenv run black src tests

lint: format
	pipenv run flake8 src/* tests/*
	pipenv run mypy src/*.py tests/*.py

test:
	pipenv run pytest --cov=src --cov-report=xml

coverage-html:
	pipenv run pytest --cov=src --cov-report=html

pr: lint test security-baseline complexity-baseline

audit:
	pipenv run safety check

outdated:
	pipenv update --outdated

update-requirements:
	pipenv requirements > src/requirements.txt

security-baseline:
	pipenv run bandit -r src

complexity-baseline:
	$(info Maintainability index)
	pipenv run radon mi src
	$(info Cyclomatic complexity index)
	pipenv run xenon --max-absolute C --max-modules A --max-average A src

docs-api-local:
	pipenv run pdoc --http : src

clean:
	rm -Rf .mypy_cache
	rm -Rf .pytest_cache tests/.pytest_cache
	rm -Rf .coverage coverage.xml htmlcov
	pipenv --rm
	cd iac; ./gradlew clean; rm -Rf cdk/cdk.out
