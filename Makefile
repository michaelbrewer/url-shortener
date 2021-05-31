export TABLE_NAME=table

target:
	@$(MAKE) pr

dev:
	pip3 install --upgrade pip pipenv pre-commit
	pipenv install --dev --pre
	pre-commit install
	pre-commit install --hook-type commit-msg

format:
	pipenv run isort src tests
	pipenv run black src tests

lint: format
	pipenv run flake8 src/* tests/*

test:
	pipenv run pytest --cov=src --cov-report=xml

coverage-html:
	pipenv run pytest --cov=src --cov-report=html

pr: lint test security-baseline complexity-baseline

security-baseline:
	pipenv run bandit -r src

complexity-baseline:
	$(info Maintainability index)
	pipenv run radon mi src
	$(info Cyclomatic complexity index)
	pipenv run xenon --max-absolute C --max-modules A --max-average A src

update-requirements:
	pipenv lock -r > src/requirements.txt
