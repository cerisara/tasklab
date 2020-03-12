from flask import Flask, json, request

companies = [{"id": 1, "name": "Company One"}, {"id": 2, "name": "Company Two"}]

api = Flask(__name__)

@api.route('/c', methods=['GET'])
def get_companies():
  return json.dumps(companies)

@api.route('/c', methods=['POST'])
def post_companies():
	language = request.args.get('l')
	return json.dumps({"success": True, "lg":language}), 201

if __name__ == '__main__':
    api.run(host="0.0.0.0", port=7937)

